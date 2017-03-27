#-*- coding: utf-8 -*-

from sets import Set
import IOTools # for testing
from optparse import OptionParser
import re
import sys

EQUIVALENT_TO = 'equivalent_to'
POSSIBLY_SUBSTITUTE_OF = 'possibly_substitute_of'
POSSIBLY_SUBSTITUTED_BY = 'possibly_substituted_by'
TYPE_OF = 'type_of'
FAMILY_OF = 'family_of'

def inverseRelation(relation):
  if relation == EQUIVALENT_TO:
    return EQUIVALENT_TO
  if relation == POSSIBLY_SUBSTITUTE_OF:
    return POSSIBLY_SUBSTITUTED_BY
  if relation == POSSIBLY_SUBSTITUTED_BY:
    return POSSIBLY_SUBSTITUTE_OF
  if relation == TYPE_OF:
    return FAMILY_OF
  if relation == FAMILY_OF:
    return TYPE_OF

  raise(Exception('Cannot invert relation %s' % relation))

class RDF:
  def __init__(self, origin = None, x = None, relation = None, y = None, json = None):
    if json is not None:
      self.x = json['x']
      self.y = json['y']
      self.relation = json['relation']
      self.origin = json['origin']
    else:
      self.x = x
      self.y = y
      self.relation = relation
      self.origin = origin
  def serialize(self):
    return {'x' : self.x, 'y' : self.y, 'relation' : self.relation, 'origin' : self.origin}


def add_to_set_or_create(dict_, key, item):
  if key in dict_:
    dict_[key].add(item)
  else:
    dict_[key] = Set([item])

class RDFNode:
  def __init__(self, name):
    self.name = name
    self.edges = {}

  def addEdge(self, relation, neighbor, transitive_close = False):

    add_to_set_or_create(self.edges, relation, neighbor)

    # in case of equivalence, perform transitive closure
    # this words because we assume (inductively) that equivalence-connected component
    # of both nodes was already processed
    if transitive_close:
      if relation == EQUIVALENT_TO:
        component1 = self.edges[EQUIVALENT_TO] if EQUIVALENT_TO in self.edges else Set([])
        component2 = neighbor.edges[EQUIVALENT_TO] if EQUIVALENT_TO in neighbor.edges else Set([])
        edge_types1 = self.edges.keys()
        edge_types2 = neighbor.edges.keys()
        for node in component1:
          for edge_type in edge_types2:
            if not edge_type in node.edges:  node.edges[edge_type] = Set([])
            node.edges[edge_type] = node.edges[edge_type] | neighbor.edges[edge_type]
        for  node in component2:
          for edge_type in edge_types1:
            if not edge_type in node.edges:  node.edges[edge_type] = Set([])
            node.edges[edge_type] = node.edges[edge_type] | self.edges[edge_type]
       
  def getNeighbors(self, edge_type):
    if edge_type in self.edges:
      return self.edges[edge_type]
    else: return Set([])
  
  def getNeighborsNeighbors(self, edge_type):
    neighbors = self.getNeighbors(edge_type)
    tmp_neighbors = []
    for neighbor in neighbors:
      tmp_neighbors.append(neighbor)
    for neighbor in tmp_neighbors:
      neighbors = neighbors | neighbor.getNeighbors(edge_type)
    return neighbors

  def pprint(self, indent=0):
    for relation in self.edges:
      print ' '*indent + relation + ':'
      for neighbor in self.edges[relation]:
        print ' '*(indent+2) + neighbor.name

class RDFGraph:
  def __init__(self, rdfs = [], entity_cannonicalizer = lambda x:x):
    self.nodes = {}

    # Create all nodes
    for rdf in rdfs:
      x_ = entity_cannonicalizer(rdf.x)
      x = x_ if x_ is not None else rdf.x
      y_ = entity_cannonicalizer(rdf.y)
      y = y_ if y_ is not None else rdf.y
      if x not in self.nodes and x != '':# why does '' happen?
        self.nodes[x] = RDFNode(x)
      if y not in self.nodes and y != '':# why does '' happen?
        self.nodes[y] = RDFNode(y)
    
    # Connect the edges
    for rdf in rdfs:
      x_ = entity_cannonicalizer(rdf.x)
      x = x_ if x_ is not None else rdf.x
      y_ = entity_cannonicalizer(rdf.y)
      y = y_ if y_ is not None else rdf.y
      if x == '' or y == '': continue # why does this happen?
      node_x = self.nodes[x]
      node_y = self.nodes[y]
      node_x.addEdge(rdf.relation, node_y)
      node_y.addEdge(inverseRelation(rdf.relation), node_x)

  def getNode(self, name):
    if name in self.nodes: return self.nodes[name]
    return None

  def pprint(self):
    for node_name in self.nodes:
      print node_name
      self.nodes[node_name].pprint(indent=2)


    
if __name__ == '__main__':
  parser = OptionParser()
  rdb = IOTools.RecipeDB(option_parser = parser)

  rdfs = rdb.readRDFs(origin = 'foodsubs') 
  rdfs = filter(lambda x: x.x != '' and x.y != '' and re.search('[(.:)+]|kamut', x.x + x.y) is None, rdfs)
  graph = RDFGraph(rdfs)

  graph.pprint()

  while True:
    s = sys.stdin.readline()[:-1]
    if not s in graph.nodes:
      print 'Not in graph'
      continue
    graph.nodes[s].pprint()
    print 'Neighbors of neighbors:'
    print map(lambda x: x.name, graph.nodes[s].getNeighborsNeighbors(TYPE_OF))

