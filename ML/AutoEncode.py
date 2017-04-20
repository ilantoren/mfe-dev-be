import tensorflow as tf
import numpy as np
import IOTools
from optparse import OptionParser
from IOTools import clean_str
from RDF import *
import sys

MAX_EXP = np.exp(5)
DEFAULT_TAGS = ["is_tabouli", "is_salad", "is_muffin", "is_quiche", "is_general_baking"]

def permute(array, perm):
  ret = []
  for i in range(len(array)):
    ret.append(array[perm[i]])
  return ret

class AutoEncodeRecipes:

  def getEffectiveDim(self):
    return self.dim + (self.usda_group_dim if self.use_usda_group else 0)

  def buildModel(self):
    layer_widths = self.layer_widths
    print ("layer_widths = %s" % layer_widths.__str__())
    effective_dim = self.getEffectiveDim()
    print("effective_dim = %d" % effective_dim)
    weights = []
    weights.append(tf.Variable(tf.random_normal([effective_dim, layer_widths[0]])))
    for i in range(1, len(layer_widths)):
      weights.append(tf.Variable(tf.random_normal([layer_widths[i-1], layer_widths[i]], stddev = self.matrix_weight_std)))
    weights.append(tf.Variable(tf.random_normal([layer_widths[-1], effective_dim], stddev = self.bias_weight_std)))

    biases = []
    for i in range(len(layer_widths)):
      biases.append(tf.Variable(tf.random_normal([layer_widths[i]], stddev = self.bias_weight_std)))
    biases.append(tf.Variable(tf.random_normal([effective_dim], stddev = self.bias_weight_std)))

    x = tf.placeholder(tf.float32, [None, effective_dim])
    layers = []
    if self.use_sigmoid:
      layers.append(tf.nn.sigmoid(tf.add(tf.matmul(x, weights[0]), biases[0])))
    else:
      layers.append(tf.add(tf.matmul(x, weights[0]), biases[0]))

    for i in range(1,len(layer_widths)):
      if self.use_sigmoid:
        layers.append(tf.nn.sigmoid(tf.add(tf.matmul(layers[i-1], weights[i]), biases[i])))
      else:
        layers.append(tf.add(tf.matmul(layers[i-1], weights[i]), biases[i]))

    print ("loss = %s" % self.loss)
    if self.loss == "RMSE":
      if self.use_sigmoid:
        y = tf.nn.sigmoid(tf.add(tf.matmul(layers[-1], weights[-1]), biases[-1]))
      else:
        y = tf.add(tf.matmul(layers[-1], weights[-1]), biases[-1])
    else:  # cross entropy
      y = tf.add(tf.matmul(layers[-1], weights[-1]), biases[-1])
 
    if self.loss == "RMSE":
      cost_vec = tf.reduce_mean(tf.pow(y-x, 2), reduction_indices=[1])
      cost = tf.reduce_mean(cost_vec)
    else:
      cost_vec = tf.reduce_mean(tf.nn.sigmoid_cross_entropy_with_logits(logits=y, labels=x), reduction_indices=[1])
      cost = tf.reduce_mean(cost_vec)

    regularizer = tf.reduce_mean(tf.pow(weights[0], 2))
    for i in range(1, len(weights)):
      regularizer = tf.add(regularizer, tf.reduce_mean(tf.pow(weights[i], 2)))
    for i in range(len(biases)):
      regularizer = tf.add(regularizer, tf.reduce_mean(tf.pow(biases[i], 2)))

    regularized_cost = tf.add(cost, self.regularizer_coeff * regularizer)


    # Supervised learning of the tags (when available) using weights
    # going from the last layer to the tag vector, together with bias terms
    tags = tf.placeholder(tf.float32, [None, self.num_tags_to_learn])
    tag_masks = tf.placeholder(tf.float32, [None, self.num_tags_to_learn])
    tag_weights = tf.Variable(tf.random_normal([layer_widths[-1], self.num_tags_to_learn], stddev = self.matrix_weight_std))
    tag_biases = tf.Variable(tf.random_normal([self.num_tags_to_learn], self.bias_weight_std))
    tag_prediction = tf.add(tf.matmul(layers[-1], tag_weights), tag_biases)
    tag_costs_vec = tf.multiply(tag_masks, tf.nn.sigmoid_cross_entropy_with_logits(logits=tag_prediction, labels=tags))
    tag_total_cost = tf.divide(tf.reduce_sum(tag_costs_vec), tf.reduce_sum(tag_masks)+0.001)
    tag_binary_costs_vec = tf.multiply(
      tag_masks, 
      tf.abs(
        tf.add(
          (tf.sign(tag_prediction)+1)/2.0, 
          -tf.round(tags))))
    tag_binary_total_cost = tf.divide(
      tf.reduce_sum(tag_binary_costs_vec, reduction_indices=[0]), 
      tf.reduce_sum(tag_masks, reduction_indices=[0]))

    tag_labels_prob_of_one = tf.divide(
      tf.reduce_sum(tags, reduction_indices=[0]),
      tf.reduce_sum(tag_masks, reduction_indices=[0]))

    regularized_cost_with_supervised = tf.add(regularized_cost, self.supervised_coeff * tag_total_cost)

    #optimizer = tf.train.GradientDescentOptimizer(self.learning_rate).minimize(cost)
    optimizer = tf.train.RMSPropOptimizer(self.learning_rate).minimize(regularized_cost_with_supervised)  
    self.x = x
    self.layers = layers
    self.y = y
    self.optimizer = optimizer
    self.cost_vec = cost_vec
    self.cost = cost
    self.weights = weights
    self.biases = biases
    self.layers = layers
    self.tags = tags
    self.tag_masks = tag_masks
    self.tag_prediction = tag_prediction
    self.tag_costs_vec = tag_costs_vec
    self.tag_total_cost = tag_total_cost
    self.tag_binary_costs_vec = tag_binary_costs_vec
    self.tag_binary_total_cost = tag_binary_total_cost
    self.tag_labels_prob_of_one = tag_labels_prob_of_one
  def readData(self):
    for recipe in rdb.findRecipes(limit=self.limit):
      ings = recipe.getIngs()
      curr_vec = [0] * self.dim
      curr_foodgroup_vec = [0] * self.usda_group_dim
      for ing in ings:
        cann = ing["cannonical"]
        if cann is None:
          if self.debug: print("Warning: cannonical None for food %s " % ing["food"])
          continue
        if not cann in self.ing2ind:
          if self.debug: print("Cannonical %s not in ingredient_frequency table" % cann)
          continue

        curr_vec[self.ing2ind[cann]] =  curr_vec[self.ing2ind[cann]] + 1
        foodgroup = rdb.getEntityUSDAFamily(cann)
        if foodgroup is not None:
          if not foodgroup in self.foodgroup2ind:
            self.foodgroup2ind[foodgroup] = len(self.foodgroup2ind)
          curr_foodgroup_vec[self.foodgroup2ind[foodgroup]] = curr_foodgroup_vec[self.foodgroup2ind[foodgroup]] + 1

        # use rdf sub info
        node = self.rdf_graph.getNode(cann)
        if node is not None:
          neighbors = node.getNeighbors(POSSIBLY_SUBSTITUTED_BY)
          for neighbor in neighbors:
            cann_neighbor = rdb.cannonicalize_entity(neighbor.name)
            if cann_neighbor is not None and cann_neighbor in self.ing2ind and cann_neighbor != cann:
              #print cann + ' -> ' + cann_neighbor
              curr_vec[self.ing2ind[cann_neighbor]] = curr_vec[self.ing2ind[cann_neighbor]] + self.smooth_for_subs

      if sum(curr_vec) < self.min_ings_for_inclusion: continue
      self.recipe_vecs.append(curr_vec)
      self.foodgroup_vecs.append(curr_foodgroup_vec)
      self.recipe_ids.append(recipe.getId())
      curr_recipe_tags = []
      curr_recipe_tag_masks = []
      for tag in self.tags_to_learn:
        p = recipe.getTagProb(tag)
        curr_recipe_tags.append(0 if p is None else p)
        curr_recipe_tag_masks.append(0 if p is None else 1)
      self.recipe_tags.append(curr_recipe_tags)
      self.recipe_tag_masks.append(curr_recipe_tag_masks)
      self.recipe_titles.append(recipe.getTitle())
      self.recipe_tags_raw.append(recipe.getTags())

    self.n = len(self.recipe_ids)
    self.n_train = int(self.n * self.train_size)
    print("Created %d recipe vectors" % self.n)
    print("Training on %d recipe vectors" % self.n_train)
    print('recipe_tags len=%d' % len(self.recipe_tags))
    print('recipe_tag_masks len=%d' % len(self.recipe_tag_masks))

    perm = np.random.permutation(self.n)
    self.recipe_vecs = permute(self.recipe_vecs, perm)
    self.foodgroup_vecs = permute(self.foodgroup_vecs, perm)
    self.recipe_ids = permute(self.recipe_ids, perm)
    self.recipe_titles = permute(self.recipe_titles, perm)    
    self.recipe_tags = permute(self.recipe_tags, perm)
    self.recipe_tag_masks = permute(self.recipe_tag_masks, perm)
    self.recipe_tags_raw = permute(self.recipe_tags_raw, perm)

  # The loss is either "cross_entropy" or "RMSE"
  # Smooth for subs:  Whenever there 
  def __init__(self, rdb, layer_widths = [100, 20, 100], 
               learning_rate = 0.01, batch_size=10, batches_per_epoch=10000, epochs=10, mult_weights_coeff=0.0,
               min_ings_for_inclusion=5, debug=False, limit=0, use_usda_group=True, 
               loss="cross_entropy", train_size=0.5, smooth_for_subs=0.0,
               use_sigmoid = True,
               model_filename='data/autoencode_model', model_read=False, model_write=True, svd=0,
               matrix_weight_std=0.1, bias_weight_std = 0.1, regularizer_coeff = 1, supervised_coeff = 1,
               tags_to_learn = DEFAULT_TAGS):
    ingredient_freq = rdb.getStats("ingredient_frequency")
    ind = 0
    self.ing2ind = {}
    self.ind2ing = {}
    for ing in ingredient_freq:
      self.ing2ind[ing] = ind
      self.ind2ing[ind] = ing
      ind = ind + 1
    self.dim = ind

    self.tags_to_learn = tags_to_learn
    self.num_tags_to_learn = len(tags_to_learn)
    self.regularizer_coeff = regularizer_coeff
    self.supervised_coeff = supervised_coeff
    self.matrix_weight_std = matrix_weight_std
    self.bias_weight_std = bias_weight_std
    self.use_sigmoid = use_sigmoid
    self.mult_weights_coeff = mult_weights_coeff
    self.batches_per_epoch=batches_per_epoch
    self.model_filename = model_filename
    self.model_read = model_read
    self.model_write = model_write
    self.smooth_for_subs = smooth_for_subs
    self.recipe_vecs = []
    self.foodgroup_vecs = []
    self.recipe_ids = []
    self.recipe_titles = []
    self.recipe_tags = []
    self.recipe_tag_masks = []
    self.recipe_tags_raw = []
    self.debug = debug
    self.loss = loss
    self.use_usda_group = use_usda_group
    assert loss=="cross_entropy" or loss=="RMSE"
    self.usda_group_dim = rdb.getNumFoodGroups()
    if use_usda_group:
      print("Number of foodgroups = %d" % self.usda_group_dim)
    self.foodgroup2ind = {}
    self.layer_widths = layer_widths
    self.learning_rate = learning_rate
    self.debug = debug
    self.batch_size = batch_size
    self.epochs = epochs
    self.rdb = rdb
    self.limit = limit
    self.svd = svd
    self.min_ings_for_inclusion = min_ings_for_inclusion
    self.rdf_graph = RDFGraph(rdb.readRDFs(['foodsubs', 'mfe']))
    self.train_size = train_size
    self.readData()     
    if svd == 0:
      self.buildModel()      

  def getX(self, index):
    return self.recipe_vecs[index] + (self.foodgroup_vecs[index] if self.use_usda_group else [])


  def SVDTrain(self):
    mat = []
    for i in range(self.n_train):
      mat.append(self.getX(i))
    self.ut,self.s,self.v = np.linalg.svd(np.array(mat), full_matrices = False)

  def Train(self):
    
    print ("mult weights coeff=%f" % self.mult_weights_coeff)
    if self.svd > 0:
      self.SVDTrain()
      return

    init = tf.global_variables_initializer()
    saver = tf.train.Saver()

    self.sess = tf.Session()

    if self.model_read:
      print ("Initializing model with restored data in %s" % self.model_filename)
      saver.restore(self.sess, self.model_filename)
    else:
      print ("Initializing model randomly, because model_read parameter is False.")
      self.sess.run(init)    

    if self.epochs == 0: # just reading from file - an odd way to get values of variables
      result = [None, None] + self.sess.run(self.weights + self.biases)

    uniform_weight_vec = np.array([(1.0/self.n_train)] * self.n_train)
    weight_vec = uniform_weight_vec
    acc_loss_vec = [0] * self.n_train

    for _ in range(self.epochs):
      print("Epoch = %d" % _)
      ibatch = 0
      batch_indices = np.random.choice(range(self.n_train), self.batch_size * self.batches_per_epoch, p=weight_vec/sum(weight_vec))
      print("batch_indices[:10] = %s" % batch_indices[:10].__str__())
      for __ in range(self.batches_per_epoch):
        sys.stdout.write("\rEpoch %d batch %d" % (_, __))
        #if (ibatch * self.batch_size) >= self.n_train: break
        batch = []
        tag_batch = []
        tag_masks_batch = []
        for j in range(self.batch_size):
          ind = __ * self.batch_size + j
          batch.append(self.getX(batch_indices[ind])) 
          tag_batch.append(self.recipe_tags[batch_indices[ind]])
          tag_masks_batch.append(self.recipe_tag_masks[batch_indices[ind]])
        result = self.sess.run([self.optimizer, self.cost] + self.weights + self.biases, feed_dict={self.x: batch, self.tags: tag_batch, self.tag_masks: tag_masks_batch})
      
      # compute loss on everything
      print ("")
      print ("Computing total loss")
      cost_vec, cost, y, tag_total_cost, tag_binary_total_cost, tag_labels_prob_of_one= self.sess.run(
        [self.cost_vec, self.cost, self.y, self.tag_total_cost, self.tag_binary_total_cost, self.tag_labels_prob_of_one],
        feed_dict={self.x : map(lambda i: self.getX(i), range(self.n_train)), self.tags : self.recipe_tags[:self.n_train], self.tag_masks : self.recipe_tag_masks[:self.n_train]})
      print ('cost_vec=', cost_vec)
      print ('cost    =', cost)
      print ('tag_total_cost    =', tag_total_cost)
      print ('tag_binary_total_cost    =', tag_binary_total_cost)
      print ('tag_labels_prob_of_one =', tag_labels_prob_of_one)
      print ('y= ', y)
      
      # update weights
      weight_vec = weight_vec * np.exp(cost_vec * self.mult_weights_coeff)
      weight_vec = weight_vec / min(weight_vec)
      for k in range(self.n_train):
        if weight_vec[k] > MAX_EXP: weight_vec[k] = MAX_EXP


      self.result_weights = result[2:(2+len(self.weights))]
      self.result_biases = result[(2+len(self.weights)):]
      if self.model_write:
        save_path = saver.save(self.sess, self.model_filename)
        print ("Saved model to file %s" % save_path)
      else:
        print ("Model not saved because model_write parameter is False")


  def SVDpredict(self, index_set):
    print self.ut.shape
    print self.v.shape
    s = [0] * len(self.s)
    for i in range(self.svd): s[i] = self.s[i]
    reduced = np.dot(np.dot(self.ut, np.diag(s)), self.v)
    ret = []
    for i in index_set:
      ret.append(reduced[i])
    return ret	
      
  def predict(self, index_set = None):
    if index_set is None:
      index_set = range(self.n_train, self.n)
    if self.svd > 0: return self.SVDpredict(index_set)

    x = []
    for i in index_set:
      x.append(self.getX(i))
    #init = tf.global_variables_initializer()
    result_test_y, tag_predictions = self.sess.run([self.y, self.tag_prediction], feed_dict = { self.x : x})
    return result_test_y, tag_predictions

  def DebugDump(self, fn, index_set):
    f = open(fn, "wt")
    result_test_y, tag_prediction = self.predict(index_set = range(self.n))
    for i in index_set:
      f.write('%s\n%s\n' % (self.recipe_ids[i], clean_str(self.recipe_titles[i])))
      for j in range(self.dim):
        if self.recipe_vecs[i][j] == 1: f.write('  %s\n' % clean_str(self.ind2ing[j]))
      y = result_test_y[i]
      f.write('%s\n' % y.__str__())
      pairs = []
      for _ in range(self.dim): pairs.append((-y[_],_))
      pairs.sort()
      for k in range(100):
        f.write('%s:%f ' % (clean_str(self.ind2ing[pairs[k][1]]), -pairs[k][0]))
      f.write('.....')
      for k in range(100):
        f.write('%s:%f ' % (clean_str(self.ind2ing[pairs[-k][1]]), -pairs[-k][0]))
      f.write("\n")
      f.write("Original tags:\n")
      f.write(self.recipe_tags_raw[i].__str__())
      f.write("\nPredicted tags:\n")
      for k in range(self.num_tags_to_learn):
        f.write("%s: %f\n" % (self.tags_to_learn[k], tag_prediction[i][k]))
    f.close()

if __name__ == "__main__":
  parser = OptionParser()
  parser.add_option("-d", "--debug", dest="debug", help="Debug mode (default False)", default="False", metavar="DEBUG")
  parser.add_option("--mi", "--min_ingredients", dest="min_ingredients", help="Min number of ingredients for inclusion in dataset (default=4)", default="4", metavar="MIN_INGREDIENTS")
  parser.add_option("-l", "--limit", dest="limit", help="Limit number of recipes (default 0, no limit)", default="0", metavar="LIMIT")
  parser.add_option("-e", "--epochs", dest="epochs", help="Number of epochs (default 10)", default="10", metavar="EPOCH")
  parser.add_option("--bs", "--batch_size", dest="batch_size", help="batch_size (default 10)", default="10", metavar="BATCH_SIZE")
  parser.add_option("--bpe", "--batches_per_epoch", dest="batches_per_epoch", help="Batches per epoch (default 100000)", default="100000", metavar="BATCHES_PER_EPOCH")
  parser.add_option("--df", "--debug_dump_filename", dest="debug_dump_filename", help="Filename to write debug dump output at end of execution (default none - no dumping)", default="data/auto_encode.txt", metavar="DEBUG_DUMP_FILENAME")
  parser.add_option("--mwc", "--mult_weights_coeff", dest="mult_weights_coeff", help="Multiplicative weights coefficients (default: 0, meaning effectively no mult. weights) ", default="0.0", metavar="MULT_WEIGHTS_COEFF")

  parser.add_option("-w", "--layer_widths", dest="layer_widths", help="Comma separated layer widths.  Default 100,20,100", default="100,20,100", metavar="LAYER_WIDTHS")
  parser.add_option("--ls" , "--loss", dest="loss", help="Loss, either cross_entropy or RMSE (default: cross_entropy)", default="cross_entropy", metavar="LOSS")
  parser.add_option("--usda" , "--use_usda", dest="use_usda", help="Add a coordinate for usda group info (default False)", default="False", metavar="USE_USDA")
  parser.add_option("--ts" , "--train_size", dest="train_size", help="Relative size of training set (default: 0.95)", default="0.95", metavar="TRAIN_SIZE")
  parser.add_option("--ss", "--smooth_for_subs", dest="smooth_for_subs", help="Smooth coefficient for ingredients that can be subs of each other (default: 0.0)", default="0.0", metavar="SMOOTH_FOR_SUBS")
  parser.add_option("--ns", "--noise_stdev", dest="noise_stdev", help="Noise stdev, for prediction, one stdev per layer (default 0.0,0.0,0.0)", default="0.0,0.0,0.0", metavar="NOISE_STDEV")
  parser.add_option("--mf", "--model_filename", dest="model_filename", help="Model file name for write/restore", default="data/autoencode_model", metavar="MODEL_FILENAME")
  parser.add_option("--mw", "--model_write", dest="model_write", help="Write model after training (default: True)", default="True", metavar="MODEL_WRITE")
  parser.add_option("--mr", "--model_read", dest="model_read", help="Read model before training (default: False)", default="False", metavar="MODEL_READ")
  parser.add_option("--lr", "--learning_rate", dest="learning_rate", help="Learning rate (default=0.01)", default="0.01", metavar="LEARNING_RATE")
  parser.add_option("--sig", "--use_sigmoid", dest="use_sigmoid", help="Add a sigmoid activation at each layer (default False)", default="False", metavar="USE_SIGMOID")
  parser.add_option("--svd", "--svdk", dest="svd", help="Autoencode using SVD of dimension k (default 0, which means no SVD)", default="0", metavar="SVDK")
  parser.add_option("--mwstd", "--matrix_weight_std", dest="matrix_weight_std", help="Matrix weight STD (default 0.1)", default="0.1", metavar="MATRIX_WEIGHT_STD")
  parser.add_option("--bwstd", "--bias_weight_std", dest="bias_weight_std", help="Bias weight STD (default 0.1)", default="0.1", metavar="BIAS_WEIGHT_STD")
  parser.add_option("--reg", "--regularizer_coeff", dest="regularizer_coeff", help="Coefficient multiplying regularizer term in optimization", default="1", metavar="REGULARIZER_COEFF")
  parser.add_option("--sup", "--supervised_coeff", dest="supervised_coeff", help="Coefficient multiplying supervised cost term", default="1", metavar="SUPERVISED_COEFF")

  rdb = IOTools.RecipeDB(option_parser = parser)
  (options, args) = parser.parse_args()
  aer = AutoEncodeRecipes(
    rdb, limit=eval(options.limit), epochs=eval(options.epochs), 
    batch_size = eval(options.batch_size),
    mult_weights_coeff = eval(options.mult_weights_coeff),
    batches_per_epoch = eval(options.batches_per_epoch),
    layer_widths = eval("["+options.layer_widths + "]"), 
    loss=options.loss, use_usda_group = eval(options.use_usda), 
    min_ings_for_inclusion=eval(options.min_ingredients),
    train_size = eval(options.train_size),
    smooth_for_subs = eval(options.smooth_for_subs), 
    model_filename = options.model_filename,
    model_write = eval(options.model_write),
    model_read = eval(options.model_read),
    learning_rate = eval(options.learning_rate),
    use_sigmoid = eval(options.use_sigmoid),
    svd = eval(options.svd),
    matrix_weight_std = eval(options.matrix_weight_std),
    bias_weight_std = eval(options.bias_weight_std),
    regularizer_coeff = eval(options.regularizer_coeff),
    supervised_coeff = eval(options.supervised_coeff))

  aer.Train()
  if options.debug_dump_filename != 'none':
    print('Dumping train output to file %s' % options.debug_dump_filename + '.train')
    aer.DebugDump(options.debug_dump_filename + '.train', range(aer.n_train))
    print('Dumping test output to file %s' % options.debug_dump_filename + '.test')
    aer.DebugDump(options.debug_dump_filename + '.test', range(aer.n_train, aer.n))


