/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.ingredient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import org.springframework.data.annotation.Id;

/**
 *
 * @author richardthorne
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "entity")
public class EntityMapping implements Serializable  {
    
    @Id
    private String id;
    
    @JacksonXmlProperty( localName = "entityName")
    @JsonProperty
    private String name;
    
    private String description;

    private Boolean skip = true;
    private String ndb_no;
    private String origin;
    
  @JacksonXmlElementWrapper(localName = "altNames")
    @JacksonXmlProperty
    @JsonProperty
  private final Collection<AltName> altName = new LinkedList(); 
    
  
  public EntityMapping() {}
  public EntityMapping( String name ) {
      this.name = name;
      this.altName.add(new AltName(name));
  }

    public String getName() {
        return name;
    }

    public void setName(String name) {
    	addAltName( name );
        this.name = name;
    }

    public Collection<AltName> getAltName() {
        return altName;
    }
    
    public void setAltName( Collection<AltName> set ) {
        this.altName.clear();
        this.altName.addAll(set);
    }

    public String getNdb_no() {
        return ndb_no;
    }

    public void setNdb_no(String ndb_no) {
        this.ndb_no = ndb_no;
    }
  
    
    
  public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
public void addAltName( String alt ) {
	    if (alt == null || alt.isEmpty()  ) return;
        Optional<AltName> x = getAltName().stream().filter( a -> a.getAlt().equalsIgnoreCase(alt)).findFirst();
        if (x.isPresent() ) {
            // do  not add
        }else {
                this.altName.add( new  AltName( alt ) );
        }
  }


   public void removeAltName( String alt ) {
	   AltName altName = new AltName( alt );
	   getAltName().remove( altName );
   }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
  
    
  
}
