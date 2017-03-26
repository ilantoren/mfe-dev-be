# This program is used for providing nearest neighbor tools for recipes
#

import IOTools
from optparse import OptionParser

if __name__ == "__main__":
  parser = OptionParser()
  parser.add_option("-c", "--config", dest="config", help="Mongo configuration", default="local.config", metavar="CONFIG")
  (options, args) = parser.parse_args()

  rdb = IOTools.RecipeDB(config_file = options.config)
  rdb.dropAllSubstitutionCollections
