
class Feedback:
  def __init__(self, personId = '', recipeId = '', urn = '', DBVersion = '', timestamp = '', drawDistribution = {}, taskId = '', frontEndVersion = '', instructions = '', payment = '', feedback = []):
    self.personId = personId
    self.recipeId = recipeId
    self.urn = urn
    self.DBVersion = DBVersion
    self.timestamp = timestamp
    self.drawDistribution = drawDistribution
    self.taskId = taskId
    self.frontEndVersion = frontEndVersion
    self.instructions = instructions
    self.payment = payment
    self.feedback = feedback


  # For writing as mongo object
  def getStruct(self):
    return {
      'personId'  : self.personId,
      'recipeId' : self.recipeId,
      'urn'      : self.urn,
      'DBVersion' : self.DBVersion,
      'timestamp' : self.timestamp,
      'drawDistribution' : self.drawDistribution,
      'taskId'    : self.taskId,
      'frontEndVersion' : self.frontEndVersion,
      'instructions' : self.instructions,
      'payment' : self.payment,
      'feedback' : map(lambda x: x.getStruct(), self.feedback) }

class FeedbackData:
  def __init__(self, type_ = '', ref = '', info = '', comments = ''):
    self.type = type_
    self.ref = ref
    self.info = info
    self.comments = comments

  def getStruct(self):
    return {
      'type' : self.type,
      'ref'  : self.ref,
      'info' : self.info,
      'comments' : self.comments }
