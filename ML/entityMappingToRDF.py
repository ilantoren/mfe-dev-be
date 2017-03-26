#-*- coding: utf-8 -*-

# This program converts the equivalence relation from entityMapping (via the "altName" field) to RDF info
# of origin entityMapping

from sets import Set
import IOTools # for testing
from optparse import OptionParser
import re
import sys
import RDF


ENTITY_MAPPING_RDF_ORIGIN='entityMapping'
    
if __name__ == '__main__':
  parser = OptionParser()
  rdb = IOTools.RecipeDB(option_parser = parser)

  entitymap_alt_dic = rdb.get_entitymap_alt_dic()
  rdfs = []
  for alt in entitymap_alt_dic:
    ent = entitymap_alt_dic[alt]
    if ent == alt: continue
    if ent == '' or alt == '': continue
    rdfs.append(RDF.RDF(origin=ENTITY_MAPPING_RDF_ORIGIN, x=alt, relation = RDF.EQUIVALENT_TO, y=ent))

  rdb.removeRDFsFromOrigin(ENTITY_MAPPING_RDF_ORIGIN)  
  rdb.writeRDFs(rdfs)
