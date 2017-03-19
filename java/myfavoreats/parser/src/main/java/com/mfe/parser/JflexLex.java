
package com.mfe.parser;

import com.mfe.parser.RecipeToken;
import com.mfe.parser.TokenClass;
import java.io.IOException;

/**
 *
 * @author richardthorne
 */


public interface JflexLex {
      RecipeToken getToken( TokenClass sym);
      RecipeToken nextToken() throws IOException;  

}
