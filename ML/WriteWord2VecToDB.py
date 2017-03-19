#-*- coding: utf-8 *-*
	
#
# This program reads a textual word2vec output, and writes it to the DB.
# It is basically a wrapper for a function in the RecipeDB class from IOTools
#

import sys
from IOTools import *
import re
from sets import Set
from optparse import OptionParser


if __name__ == "__main__":

  parser = OptionParser()
  parser.add_option("-f", "--file", dest="filename", help="Input FILE", default='data/word2vec.ing.permute.out.txt', metavar="FILE")
  parser.add_option("-v", "--vecname", dest="vecname", help="A tag for the vecs", default="word2vec")
  rdb = RecipeDB(option_parser = parser)  
  (options, args) = parser.parse_args()
  rdb.writeWord2VecToDB(filename = options.filename, vecname=options.vecname)

