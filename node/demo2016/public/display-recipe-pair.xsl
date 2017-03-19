
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:cc="http://creativecommons.org/ns#"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:svg="http://www.w3.org/2000/svg"
	xmlns="http://www.w3.org/2000/svg"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:prop="http://saxonica.com/ns/html-property"
	xmlns:style="http://saxonica.com/ns/html-style-property"
	xmlns:ixsl="http://saxonica.com/ns/interactiveXSLT"
	xmlns:foo="http://foo.mfe.com"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	exclude-result-prefixes="xs prop"
	extension-element-prefixes="ixsl"
	version="2.0">

	<xsl:output
		method="xml"
		encoding="utf-8"
		indent="yes"
		media-type="image/svg+xml" />
	<xsl:param
		name="title">blank</xsl:param>
	<xsl:variable
		name="recipe_title"
		select="$title" />


	<xsl:template
		match="/ArrayList">
		<root>
			<div>
<!--  <xsl:result-document href="#tabs-child-2" method="ixsl:replace-content">-->
				<xsl:apply-templates
					select="item[1]/nutrients"
					mode="child">
					<xsl:with-param
						name="display"
						select="item[1]/recipeChange"/>
				</xsl:apply-templates> 
<!--  </xsl:result-document>-->
			</div>

			<div>
<!--  <xsl:result-document href="#tabs-child-1" method="ixsl:replace-content">-->
				<xsl:apply-templates
					select="item[1]/steps/steps[1]/lines"
					mode="child">
					<xsl:with-param
						name="display"
						select="item[1]/recipeChange"/>
					<xsl:with-param
						name="title"
						select="item[1]/substitutionRule" />
					<xsl:with-param
						name="id"
						select="item[1]/parentId" />
				</xsl:apply-templates> 
<!--  </xsl:result-document>-->
			</div>

			<div>
<!--  <xsl:result-document href="#tabs-orig-2" method="ixsl:replace-content">-->
				<xsl:apply-templates
					select="item[2]/nutrients"
					mode="parent">
					<xsl:with-param
						name="display"
						select="item[1]/recipeChange"/>
				</xsl:apply-templates> 
  <!--</xsl:result-document>-->
			</div>

			<div>
<!--  <xsl:result-document href="#tabs-orig-1" method="ixsl:replace-content">-->
				<xsl:apply-templates
					select="item[2]/steps/steps[1]/lines"
					mode="parent">
					<xsl:with-param
						name="display"
						select="item[1]/recipeChange"/>
					<xsl:with-param
						name="title"
						select="item[2]/title"/>
					<xsl:with-param
						name="id"
						select="item[2]/id"/>
				</xsl:apply-templates> 
<!--  </xsl:result-document>-->
			</div>
			
			<div id="notes">
			<h3>notes</h3>
			<ul>
			  <xsl:for-each select="//item[2]/subs/subs/options/options/moreinfo">
			     <li><xsl:value-of select="."/></li>
			     </xsl:for-each>
			</ul>
			</div>
		</root>
	</xsl:template>



<!-- child ingredient list -->
	<xsl:template match="lines"
		mode="child">
		<xsl:param
			name="display"/>
		<xsl:param
			name="title"/>
		<xsl:param
			name="id"/>
		<xsl:apply-templates
			select=".">
			<xsl:with-param
				name="display"
				select="$display"></xsl:with-param>
			<xsl:with-param
				name="title"
				select="$title" />
			<xsl:with-param
				name="id"
				select="$id"/>
			<xsl:with-param
				name="isChild">child</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>

<!-- parent ingredient list -->
	<xsl:template
		match="lines"
		mode="parent">
		<xsl:param
			name="display"/>
		<xsl:param
			name="title" />
		<xsl:param
			name="id" />
		<xsl:apply-templates
			select=".">
			<xsl:with-param
				name="display"
				select="$display"></xsl:with-param>
			<xsl:with-param
				name="title"
				select="$title" />
			<xsl:with-param
				name="id"
				select="$id"/>
			<xsl:with-param
				name="isChild">parent</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>



<xsl:template match="substitution">
  <xsl:param name="parentId" />
  <xsl:for-each select="./subs">


     <xsl:sort order="descending" select="carbs" data-type="number" />

      <xsl:sort select="choChange"  data-type="number"/>

      <li>
      	<xsl:element name="a">
      	 <xsl:attribute name = "id"><xsl:value-of select="$parentId" /></xsl:attribute>
      	  <xsl:attribute name="name" ><xsl:value-of select="targetId"/></xsl:attribute>
      	 <xsl:attribute name="href">#substitute</xsl:attribute>
      	 <xsl:attribute name="onclick">selectSubstitute(this)</xsl:attribute>
      	 <!--  xsl:value-of select="concat( fn:replace(description, '/^for /',''),  '   ' ,  '% change in carbs: ', choChange)" / -->
      	 <xsl:value-of select="fn:replace(description, '/^for /','')" />
      	
      	 </xsl:element>
      </li>
    </xsl:for-each>
</xsl:template>

	<xsl:template
		match="lines">
		<xsl:param
			name="display"></xsl:param>
		<xsl:param
			name="title">blank</xsl:param>
		<xsl:param
			name="id" />
		<xsl:param
			name="isChild"/>
		<div>
			<xsl:attribute
				name="name">
				<xsl:value-of
					select="$id"/>
			</xsl:attribute>
			<xsl:choose>
				<xsl:when
					test="$isChild = 'child'">
					<div
						class="dropdown"
						id="substitutions-drpdwn">
						<button
							class="btn btn-default dropdown-toggle"
							type="button"
							id="dropdownMenu2"
							data-toggle="dropdown"
							aria-haspopup="true"
							aria-expanded="true">
							<xsl:value-of
								select="$title"/>
							<span
								class="caret"></span>
						</button>
						<ul
							id="dropdowndynamic"
							class="dropdown-menu"
							aria-labelledby="dropdownMenu1"></ul>
					</div>
				</xsl:when>
				<xsl:otherwise>
					<h3
						id="titlex">
						<xsl:value-of
							select="$title" />
					</h3>
				</xsl:otherwise>
			</xsl:choose>
    

			<table>
				<thead>
					<tr>
  <!--th class="amnt">amount</th><th class="unit">(g)</th -->
                        <th class="amnt"></th>
                        <th class="measure"></th>
						<th
							class="ingredient">ingredient</th>
					</tr>
				</thead>
				<tbody>
					<xsl:for-each
						select="lines">
						<xsl:choose>
							<xsl:when
								test="$isChild = 'parent' and substitution/subs">
								<xsl:variable
									name="pattern"
									select="food"/>
								<xsl:variable
									name="parts"
									select="fn:tokenize( food, $pattern)" />
								<tr class="decreased">
								    <td class="amnt"><xsl:value-of select="quantity"/></td>
								    <td class="measure"><xsl:value-of select="measure"/></td>
									<td>
										<span>
											<xsl:value-of
												select="$parts[1]"/>
										</span>
										<xsl:element
											name="div">
											<xsl:attribute
												name="class">btn-group</xsl:attribute>
											<button
												type="button"
												class="btn btn-default dropdown-toggle"
												data-toggle="dropdown"
												aria-haspopup="true"
												aria-expanded="false">
												<xsl:value-of
													select="$pattern" />
											</button>
											<ul
												class="dropdown-menu">
												<xsl:apply-templates
													select="substitution" >
													<xsl:with-param name="parentId" select="$id"/>
											    </xsl:apply-templates>
											</ul>
										</xsl:element>
										<span>
											<xsl:value-of
												select="$parts[2]" />
										</span>
									</td>
								</tr>
							</xsl:when>
							<xsl:otherwise>
								<tr>
									<xsl:choose>
										<xsl:when
											test="contains( $display/added/addedIngredients, food)">
											<xsl:attribute
												name="class">added</xsl:attribute>
										</xsl:when>
										<xsl:when
											test="contains( $display/reduced/reducedIngredients, food)">
											<xsl:attribute
												name="class">decreased</xsl:attribute>
										</xsl:when>
									</xsl:choose>
 
  <!--td><xsl:value-of select="gram"/></td>
  <td class="unit"> g </td-->        <td class="amnt"><xsl:value-of select="quantity"/></td>
								    <td class="measure"><xsl:value-of select="measure"/></td>
									<td class="ingredient">
										<xsl:value-of
											select="food"
											disable-output-escaping="yes"/>
									</td>
								</tr>
							</xsl:otherwise>
						</xsl:choose>

					</xsl:for-each>
				</tbody>
			</table>
		</div>
	</xsl:template>



	<xsl:template
		match="nutrients"
		mode="child">


		<xsl:param
			name="display">------</xsl:param>

		<xsl:variable
			name="calories"
			select="format-number(number(calories),  '#0.0 ')"/>
		<xsl:variable
			name="fatCalories"
			select="totalFat * 9" />
		<xsl:variable
			name="caloriesFat"
			select="format-number( $fatCalories, '#0.0 ')"/>
		<xsl:variable
			name="totalFat"
			select="format-number(number(totalFat), '#0.0 ')"/>
		<xsl:variable
			name="satFat"
			select="format-number(number(satFat),  '#0.0 ')"/>
		<xsl:variable
			name="transFat">
			<tspan
				id="tspan35"
				style="font-style:oblique">Trans</tspan>Fat: <xsl:value-of
				select="format-number(number(transFat)*1000, '#.#')"/>
		</xsl:variable>
		<xsl:variable
			name="cholesterol"
			select="format-number(number(cholesterol),  '#0.0 ')"/>
		<xsl:variable
			name="sodium"
			select="format-number(number(sodium),  '#0.0 ')" />
		<xsl:variable
			name="totalCarb"
			select="format-number(number(carbohydrate),  '#0')"/>
		<xsl:variable
			name="dietFiber"
			select="format-number(number(fiber),  '#0.0')"/>
		<xsl:variable
			name="sugars"
			select="format-number(number(sugars),  '#0.0')"/>
		<xsl:variable
			name="protein"
			select="format-number(number(protein),  '#0.0')"/>
<!--RDA:  totalFat: 65, satFat: 20,cholesterol: 300,  sodium: 2400, carbohydrate: 300, fiber: 25, protein: 50 -->
<!--
	// special case for transFat conver from g to mg
	// special case totalFat   9 calories per gram of fat
	// special case for sugars   2200 kcal is RDA for moderate activity  1 gram carb = 4 kcal
	-->
		<xsl:variable
			name="proteinRDA"
			select="format-number( (number(protein) div 50 *100 ),  '#0')"/>
		<xsl:variable
			name="sugarsRDA"
			select="format-number( (number(sugars)*4 div 2200 * 100),  '#0')"/>
		<xsl:variable
			name="totalCarbRDA"
			select="format-number( (number(carbohydrate) div 300 * 100),  '#0')"/>
		<xsl:variable
			name="sodiumRDA"
			select="format-number( (number(sodium) div 2400 * 100) ,  '#0')"/>
		<xsl:variable
			name="dietFiberRDA"
			select="format-number( (number(fiber) div 25 * 100),  '#0')" />
		<xsl:variable
			name="totalFatRDA"
			select="format-number( (number(totalFat) div 65 * 100),  '#0')"/>
		<xsl:variable
			name="satFatRDA"
			select="format-number( (number(satFat) div 20 * 100),  '#0')"/>
		<xsl:variable
			name="cholesterolRDA"
			select="format-number( (number(cholesterol) div 300 * 100),  '#0')"/>




		<svg
			version="1.1"
			x="0"
			y="0"
			id="svg2"
			viewBox="0 0 525 660"
			width="352.01562"
			height="437" >
			<defs
				id="defs3">
				<style
					id="style5"
					type="text/css"><![CDATA[
            .nut_header {  font-family:franklin gothic heavy, sans-serif;  fill: #000000;font-weight:900; font-size:48px}
            .nut_comment {padding-left: 12px; text-indent: -10px; font-family: Helvetica Black; font-size : 12px; }
            .nut_main { padding: 0px 0px 0px 0px; height:14px; font-family: Helvetica Regular; font-size: 17px; font-weight: bold}
            .nut_second { padding: 0px 0px 0px 14px; column-gap: 0px; font-family: Helvetica Regular; font-size: 16px; font-weight: normal}
            .red  { stroke:red; fill:red}
            .green  { stroke:green; fill:green }
            .nut_percent {padding-left:0px; column-gap: 0px; font-weight: normal }
            .advisory { height: 12px; font-family: Helvetica Regular; font-weight: normal ; font-size: 14px; padding-top: 0px; padding-bottom: 0px}   
            .nut_row { height: 13px; padding-top: 0px; padding-bottom: 0px}
            .serving { font-size: 13px }
            .nut_normal { padding: 0px 0px 0px 0px; height:14px; font-family: Helvetica Regular; font-size: 18px; font-weight: normal}
            .trans { font-weight:normal;font-size:16px;font-family:'Helvetica Regular'}
            ]]></style>
			</defs>
			<metadata
				id="metadata7">
				<rdf:RDF>
					<rdf:Description
						rdf:about="http://dbpedia.org/ontology/approximateCalories">
						<dc:format>image/svg+xml</dc:format>
						<dc:type
							rdf:resource="http://dbpedia.org/ontology/Food" />
						<dc:title>recipe</dc:title>
					</rdf:Description>
					<cc:Work
						rdf:about="">
						<dc:format>image/svg+xml</dc:format>
						<dc:type
							rdf:resource="http://purl.org/dc/dcmitype/StillImage" />
						<dc:title></dc:title>
					</cc:Work>
				</rdf:RDF>
			</metadata>
			<g
				transform="translate(-9,-9)"
				id="g8">
				<rect
					id="rect10"
					style="fill:none;fill-opacity:0;stroke:#000000;stroke-width:2px"
					height="435"
					width="350"
					y="10"
					x="10" />
				<g
					id="g12">
					<text
						style="font-weight:900;font-size:36px;font-family:'franklin gothic heavy', sans-serif;fill:#000000"
						id="text14"
						class="nut_header"
						y="60"
						x="30">Nutrition Facts</text>
					<text
						style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'"
						id="text16"
						class="nut_main"
						y="90"
						x="25">Serving Size: 100g</text>
					<text
						style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'"
						id="text18"
						class="nut_main"
						y="110"
						x="25">
						<tspan
							id="tspan20"
							style="font-weight:normal">

						</tspan>
					</text>
				</g>
				<line
					id="line22"
					style="stroke:#000000;stroke-width:7px"
					y2="130"
					x2="345"
					y1="130"
					x1="25" />
				<g
					id="g24">
					<text
						style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'"
						id="text26"
						class="nut_main"
						y="150"
						x="25">
						<tspan
							id="tspan28"
							style="font-weight:900;font-family:'helvetica black'">Amount Per Serving</tspan>
					</text>
					<text
						style="font-weight:bold;font-size:14px;font-family:'Helvetica Regular'"
						id="text30"
						class="nut_main"
						y="170"
						x="25">
						<xsl:choose>
							<xsl:when
								test="$display/calories/@changeType = 'BETTER'">
								<xsl:attribute
									name="class">nut_main green</xsl:attribute>
							</xsl:when>
							<xsl:when
								test="$display/calories/@changeType = 'WORSE'">
								<xsl:attribute
									name="class">nut_main red</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute
									name="class">nut_main</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
         Calories: <xsl:value-of
							select="$calories"/>
					</text>
					<text
						style="font-weight:bold;font-size:14px;font-family:'Helvetica Regular'"
						id="text32"
						class="nut_main"
						y="170"
						x="170">Calories from Fat: <xsl:value-of
							select="$caloriesFat"/>
					</text>
					<line
						id="line34"
						style="stroke:#808080;stroke-width:3px"
						y2="181"
						x2="345"
						y1="181"
						x1="25" />
				</g>
				<g
					id="g38">
					<text
						x="250"
						y="199"
						class="nut_comment"
						id="text40"
						style="font-size:12px;font-family:'Helvetica Black';text-indent:-10px">% Daily Values*</text>
					<line
						x1="20"
						x2="350"
						y1="205"
						y2="205"
						style="stroke:#000000;stroke-width:1px"
						id="line42" />
				</g>
				<g
					id="g3160">
					<g
						id="g44"
						transform="translate(-1.9491523,0)">
						<text
							style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'"
							id="text46"
							class="nut_main"
							y="225"
							x="25">
							<xsl:choose>
								<xsl:when
									test="$display/totaltFat/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_main green</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/totalFat/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_main red</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_main</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Total Fat: <xsl:value-of
								select="$totalFat"/>g</text>
						<text
							id="text48"
							y="225"
							x="280">
							<xsl:value-of
								select="$totalFatRDA"/>%</text>
						<line
							id="line50"
							style="stroke:#000000;stroke-width:1px"
							y2="230"
							y1="230"
							x2="350"
							x1="35" />
					</g>
					<g
						id="g52"
						transform="translate(-1.9491523,3.8436508)">
						<text
							style="font-weight:normal;font-size:16px;font-family:'Helvetica Regular'"
							id="text54"
							class="nut_second"
							y="247"
							x="35">
							<xsl:choose>
								<xsl:when
									test="$display/satFat/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_second green</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/satFat/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_second red</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_second</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Saturated Fat: <xsl:value-of
								select="$satFat"/>g</text>
						<text
							id="text56"
							y="247"
							x="280">
							<xsl:value-of
								select="$satFatRDA"/>%</text>
						<line
							id="line58"
							style="stroke:#000000;stroke-width:1px"
							y2="252"
							y1="252"
							x2="350"
							x1="35" />
					</g>
					<g
						id="g60"
						transform="translate(-1.9491523,6.6873016)">
						<text
							id="text62"
							class="nut_second trans"
							y="270"
							x="35">
							<xsl:value-of
								select="$transFat"/>mg
         </text>
						<line
							id="line66"
							style="stroke:#000000;stroke-width:1px"
							y2="275"
							y1="275"
							x2="350"
							x1="20" />
					</g>
					<g
						id="g44-1"
						transform="translate(-1.9491523,77.530962)">
						<text
							style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'"
							id="text46-7"
							class="nut_main"
							y="225"
							x="25">
							<xsl:choose>
								<xsl:when
									test="$display/cholesterol/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_main green</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/cholesterol/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_main red</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_main</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Cholesterol: <xsl:value-of
								select="$cholesterol"/>mg</text>
						<text
							id="text48-4"
							y="225"
							x="280">
							<xsl:value-of
								select="$cholesterolRDA"/>%</text>
						<line
							id="line50-0"
							style="stroke:#000000;stroke-width:1px"
							y2="230"
							y1="230"
							x2="350"
							x1="20" />
					</g>
					<g
						id="g44-9"
						transform="translate(-1.9491523,103.37458)">
						<text
							style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'"
							id="text46-4"
							class="nut_main"
							y="225"
							x="25">
							<xsl:choose>
								<xsl:when
									test="$display/sodium/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_main green</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/sodium/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_main red</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_main</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Sodium: <xsl:value-of
								select="$sodium"/>mg</text>
						<text
							id="text48-8"
							y="225"
							x="280">
							<xsl:value-of
								select="$sodiumRDA"/>%</text>
						<line
							id="line50-8"
							style="stroke:#000000;stroke-width:1px"
							y2="230"
							y1="230"
							x2="350"
							x1="20" />
					</g>
					<g
						transform="translate(-1.9491523,129.21825)"
						id="g44-7">
						<text
							x="25"
							y="225"
							class="nut_main"
							id="text46-1"
							style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'">
							<xsl:choose>
								<xsl:when
									test="$display/carbohydrate/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_main green</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/carbohydrate/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_main red</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_main</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Total Carbohydrates: 
           <xsl:value-of
								select="$totalCarb"/>g
           </text>
						<text
							x="280"
							y="225"
							id="text48-1">
							<xsl:value-of
								select="$totalCarbRDA"/>%</text>
						<line
							x1="35"
							x2="350"
							y1="230"
							y2="230"
							style="stroke:#000000;stroke-width:1px"
							id="line50-5" />
					</g>
					<g
						transform="translate(-1.9491523,155.06188)"
						id="g44-2">
						<text
							style="font-weight:normal;font-size:16px;font-family:'Helvetica Regular'"
							x="35"
							y="225"
							id="text46-45">
							<xsl:choose>
								<xsl:when
									test="$display/fiber/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_second green</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/fiber/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_second red</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_second</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Dietary Fiber: <xsl:value-of
								select="$dietFiber"/>g</text>
						<text
							x="280"
							y="225"
							id="text48-5">
							<xsl:value-of
								select="$dietFiberRDA"/>%</text>
						<line
							x1="35"
							x2="350"
							y1="230"
							y2="230"
							style="stroke:#000000;stroke-width:1px"
							id="line50-1" />
					</g>
					<g
						transform="translate(-1.9491523,180.90556)"
						id="g44-3">
						<text
							style="font-weight:normal;font-size:16px;font-family:'Helvetica Regular'"
							x="35"
							y="225"
							id="text46-2">
							<xsl:choose>
								<xsl:when
									test="$display/sugars/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_second green</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/sugars/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_second red</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_second</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Sugars: <xsl:value-of
								select="$sugars"/>g</text>
						<text
							x="280"
							y="225"
							id="text48-2">
							<xsl:value-of
								select="$sugarsRDA"/>%</text>
						<line
							x1="20"
							x2="350"
							y1="230"
							y2="230"
							style="stroke:#000000;stroke-width:1px"
							id="line50-16" />
					</g>
					<g
						transform="translate(-1.9491523,206.7492)"
						id="g44-27">
						<text
							style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'"
							x="25"
							y="225"
							id="text46-6">
							<xsl:choose>
								<xsl:when
									test="$display/protein/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_main green</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/protein/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_main red</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_main</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Protein: <xsl:value-of
								select="$protein"/>g</text>
						<text
							x="280"
							y="225"
							id="text48-14">
							<xsl:value-of
								select="$proteinRDA"/>%</text>
					</g>
				</g>
			</g>
		</svg>
	</xsl:template>

	<xsl:template
		match="nutrients"
		mode="parent">

		<xsl:param
			name="display"></xsl:param>

		<xsl:variable
			name="calories"
			select="format-number( number(calories),  '#0.0 ')"/>
		<xsl:variable
			name="fatCalories"
			select="totalFat * 9" />
		<xsl:variable
			name="caloriesFat"
			select="format-number( $fatCalories, '#0.0 ')"/>
		<xsl:variable
			name="totalFat"
			select="format-number(number(totalFat), '#0.0 ')"/>
		<xsl:variable
			name="satFat"
			select="format-number(number(satFat),  '#0.0 ')"/>
		<xsl:variable
			name="transFat">
			<tspan
				id="tspan35"
				style="font-style:oblique">Trans</tspan>Fat: <xsl:value-of
				select="format-number(number(transFat)*1000, '#.#')"/>
		</xsl:variable>
		<xsl:variable
			name="cholesterol"
			select="format-number(number(cholesterol),  '#0.0 ')"/>
		<xsl:variable
			name="sodium"
			select="format-number(number(sodium),  '#0.0 ')" />
		<xsl:variable
			name="totalCarb"
			select="format-number(number(carbohydrate),  '#0')"/>
		<xsl:variable
			name="dietFiber"
			select="format-number(number(fiber),  '#0.0')"/>
		<xsl:variable
			name="sugars"
			select="format-number(number(sugars),  '#0.0')"/>
		<xsl:variable
			name="protein"
			select="format-number(number(protein),  '#0.0')"/>
<!--RDA:  totalFat: 65, satFat: 20,cholesterol: 300,  sodium: 2400, carbohydrate: 300, fiber: 25, protein: 50 -->
<!--
	// special case for transFat conver from g to mg
	// special case totalFat   9 calories per gram of fat
	// special case for sugars   2200 kcal is RDA for moderate activity  1 gram carb = 4 kcal
	-->
		<xsl:variable
			name="proteinRDA"
			select="format-number( (number(protein) div 50 *100 ),  '#0')"/>
		<xsl:variable
			name="sugarsRDA"
			select="format-number( (number(sugars)*4 div 2200 * 100),  '#0')"/>
		<xsl:variable
			name="totalCarbRDA"
			select="format-number( (number(carbohydrate) div 300 * 100),  '#0')"/>
		<xsl:variable
			name="sodiumRDA"
			select="format-number( (number(sodium) div 2400 * 100) ,  '#0')"/>
		<xsl:variable
			name="dietFiberRDA"
			select="format-number( (number(fiber) div 25 * 100),  '#0')" />
		<xsl:variable
			name="totalFatRDA"
			select="format-number( (number(totalFat) div 65 * 100),  '#0')"/>
		<xsl:variable
			name="satFatRDA"
			select="format-number( (number(satFat) div 20 * 100),  '#0')"/>
		<xsl:variable
			name="cholesterolRDA"
			select="format-number( (number(cholesterol) div 300 * 100),  '#0')"/>











		<svg
			version="1.1"
			x="0"
			y="0"
			id="svg2"
			viewBox="0 0 525 660"
			width="352.01562"
			height="437" >
			<defs
				id="defs3">
				<style
					id="style5"
					type="text/css"><![CDATA[
            .nut_header {  font-family:franklin gothic heavy, sans-serif;  fill: #000000;font-weight:900; font-size:48px}
            .nut_comment {padding-left: 12px; text-indent: -10px; font-family: Helvetica Black; font-size : 12px; }
            .nut_main { padding: 0px 0px 0px 0px; height:14px; font-family: Helvetica Regular; font-size: 17px; font-weight: bold}
            .nut_second { padding: 0px 0px 0px 14px; column-gap: 0px; font-family: Helvetica Regular; font-size: 16px; font-weight: normal}
            .red  { stroke:red; fill:red}
            .green  { stroke:green; fill:green }
            .nut_percent {padding-left:0px; column-gap: 0px; font-weight: normal }
            .advisory { height: 12px; font-family: Helvetica Regular; font-weight: normal ; font-size: 14px; padding-top: 0px; padding-bottom: 0px}   
            .nut_row { height: 13px; padding-top: 0px; padding-bottom: 0px}
            .serving { font-size: 13px }
            .nut_normal { padding: 0px 0px 0px 0px; height:14px; font-family: Helvetica Regular; font-size: 18px; font-weight: normal}
            .trans { font-weight:normal;font-size:16px;font-family:'Helvetica Regular'}
            ]]></style>
			</defs>
			<metadata
				id="metadata7">
				<rdf:RDF>
					<rdf:Description
						rdf:about="http://dbpedia.org/ontology/approximateCalories">
						<dc:format>image/svg+xml</dc:format>
						<dc:type
							rdf:resource="http://dbpedia.org/ontology/Food" />
						<dc:title>recipe</dc:title>
					</rdf:Description>
					<cc:Work
						rdf:about="">
						<dc:format>image/svg+xml</dc:format>
						<dc:type
							rdf:resource="http://purl.org/dc/dcmitype/StillImage" />
						<dc:title></dc:title>
					</cc:Work>
				</rdf:RDF>
			</metadata>
			<g
				transform="translate(-9,-9)"
				id="g8">
				<rect
					id="rect10"
					style="fill:none;fill-opacity:0;stroke:#000000;stroke-width:2px"
					height="435"
					width="350"
					y="10"
					x="10" />
				<g
					id="g12">
					<text
						style="font-weight:900;font-size:36px;font-family:'franklin gothic heavy', sans-serif;fill:#000000"
						id="text14"
						class="nut_header"
						y="60"
						x="30">Nutrition Facts</text>
					<text
						style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'"
						id="text16"
						class="nut_main"
						y="90"
						x="25">Serving Size: 100g</text>
					<text
						style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'"
						id="text18"
						class="nut_main"
						y="110"
						x="25">
						<tspan
							id="tspan20"
							style="font-weight:normal">

						</tspan>
					</text>
				</g>
				<line
					id="line22"
					style="stroke:#000000;stroke-width:7px"
					y2="130"
					x2="345"
					y1="130"
					x1="25" />
				<g
					id="g24">
					<text
						style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'"
						id="text26"
						class="nut_main"
						y="150"
						x="25">
						<tspan
							id="tspan28"
							style="font-weight:900;font-family:'helvetica black'">Amount Per Serving</tspan>
					</text>
					<text
						style="font-weight:bold;font-size:14px;font-family:'Helvetica Regular'"
						id="text30"
						class="nut_main"
						y="170"
						x="25">
						<xsl:choose>
							<xsl:when
								test="$display/calories/@changeType = 'BETTER'">
								<xsl:attribute
									name="class">nut_main red</xsl:attribute>
							</xsl:when>
							<xsl:when
								test="$display/calories/@changeType = 'WORSE'">
								<xsl:attribute
									name="class">nut_main green</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute
									name="class">nut_main</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
         Calories: <xsl:value-of
							select="$calories"/>
					</text>
					<text
						style="font-weight:bold;font-size:14px;font-family:'Helvetica Regular'"
						id="text32"
						class="nut_main"
						y="170"
						x="170">Calories from Fat: <xsl:value-of
							select="$caloriesFat"/>
					</text>
					<line
						id="line34"
						style="stroke:#808080;stroke-width:3px"
						y2="181"
						x2="345"
						y1="181"
						x1="25" />
				</g>
				<g
					id="g38">
					<text
						x="250"
						y="199"
						class="nut_comment"
						id="text40"
						style="font-size:12px;font-family:'Helvetica Black';text-indent:-10px">% Daily Values*</text>
					<line
						x1="20"
						x2="350"
						y1="205"
						y2="205"
						style="stroke:#000000;stroke-width:1px"
						id="line42" />
				</g>
				<g
					id="g3160">
					<g
						id="g44"
						transform="translate(-1.9491523,0)">
						<text
							style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'"
							id="text46"
							class="nut_main"
							y="225"
							x="25">
							<xsl:choose>
								<xsl:when
									test="$display/totalFat/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_main red</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/totFat/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_main green</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_main</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Total Fat: <xsl:value-of
								select="$totalFat"/>g</text>
						<text
							id="text48"
							y="225"
							x="280">
							<xsl:value-of
								select="$totalFatRDA"/>%</text>
						<line
							id="line50"
							style="stroke:#000000;stroke-width:1px"
							y2="230"
							y1="230"
							x2="350"
							x1="35" />
					</g>
					<g
						id="g52"
						transform="translate(-1.9491523,3.8436508)">
						<text
							style="font-weight:normal;font-size:16px;font-family:'Helvetica Regular'"
							id="text54"
							class="nut_second"
							y="247"
							x="35">
							<xsl:choose>
								<xsl:when
									test="$display/satFat/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_second red</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/satFat/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_second green</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_second</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Saturated Fat: <xsl:value-of
								select="$satFat"/>g</text>
						<text
							id="text56"
							y="247"
							x="280">
							<xsl:value-of
								select="$satFatRDA"/>%</text>
						<line
							id="line58"
							style="stroke:#000000;stroke-width:1px"
							y2="252"
							y1="252"
							x2="350"
							x1="35" />
					</g>
					<g
						id="g60"
						transform="translate(-1.9491523,6.6873016)">
						<text
							id="text62"
							class="nut_second trans"
							y="270"
							x="35">
							<xsl:value-of
								select="$transFat"/>mg
         </text>
						<line
							id="line66"
							style="stroke:#000000;stroke-width:1px"
							y2="275"
							y1="275"
							x2="350"
							x1="20" />
					</g>
					<g
						id="g44-1"
						transform="translate(-1.9491523,77.530962)">
						<text
							style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'"
							id="text46-7"
							class="nut_main"
							y="225"
							x="25">
							<xsl:choose>
								<xsl:when
									test="$display/cholesterol/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_main red</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/cholesterol/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_main green</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_main</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Cholesterol: <xsl:value-of
								select="$cholesterol"/>mg</text>
						<text
							id="text48-4"
							y="225"
							x="280">
							<xsl:value-of
								select="$cholesterolRDA"/>%</text>
						<line
							id="line50-0"
							style="stroke:#000000;stroke-width:1px"
							y2="230"
							y1="230"
							x2="350"
							x1="20" />
					</g>
					<g
						id="g44-9"
						transform="translate(-1.9491523,103.37458)">
						<text
							style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'"
							id="text46-4"
							class="nut_main"
							y="225"
							x="25">
							<xsl:choose>
								<xsl:when
									test="$display/sodium/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_main red</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/sodium/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_main green</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_main</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Sodium: <xsl:value-of
								select="$sodium"/>mg</text>
						<text
							id="text48-8"
							y="225"
							x="280">
							<xsl:value-of
								select="$sodiumRDA"/>%</text>
						<line
							id="line50-8"
							style="stroke:#000000;stroke-width:1px"
							y2="230"
							y1="230"
							x2="350"
							x1="20" />
					</g>
					<g
						transform="translate(-1.9491523,129.21825)"
						id="g44-7">
						<text
							x="25"
							y="225"
							class="nut_main"
							id="text46-1"
							style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'">
							<xsl:choose>
								<xsl:when
									test="$display/carbohydrate/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_main red</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/carbohydrate/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_main green</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_main</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Total Carbohydrates: 
           <xsl:value-of
								select="$totalCarb"/>g
           </text>
						<text
							x="280"
							y="225"
							id="text48-1">
							<xsl:value-of
								select="$totalCarbRDA"/>%</text>
						<line
							x1="35"
							x2="350"
							y1="230"
							y2="230"
							style="stroke:#000000;stroke-width:1px"
							id="line50-5" />
					</g>
					<g
						transform="translate(-1.9491523,155.06188)"
						id="g44-2">
						<text
							style="font-weight:normal;font-size:16px;font-family:'Helvetica Regular'"
							x="35"
							y="225"
							id="text46-45">
							<xsl:choose>
								<xsl:when
									test="$display/fiber/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_second red</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/fiber/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_second green</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_second</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Dietary Fiber: <xsl:value-of
								select="$dietFiber"/>g</text>
						<text
							x="280"
							y="225"
							id="text48-5">
							<xsl:value-of
								select="$dietFiberRDA"/>%</text>
						<line
							x1="35"
							x2="350"
							y1="230"
							y2="230"
							style="stroke:#000000;stroke-width:1px"
							id="line50-1" />
					</g>
					<g
						transform="translate(-1.9491523,180.90556)"
						id="g44-3">
						<text
							style="font-weight:normal;font-size:16px;font-family:'Helvetica Regular'"
							x="35"
							y="225"
							id="text46-2">
							<xsl:choose>
								<xsl:when
									test="$display/sugars/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_second red</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/sugars/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_second green</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_second</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Sugars: <xsl:value-of
								select="$sugars"/>g</text>
						<text
							x="280"
							y="225"
							id="text48-2">
							<xsl:value-of
								select="$sugarsRDA"/>%</text>
						<line
							x1="20"
							x2="350"
							y1="230"
							y2="230"
							style="stroke:#000000;stroke-width:1px"
							id="line50-16" />
					</g>
					<g
						transform="translate(-1.9491523,206.7492)"
						id="g44-27">
						<text
							style="font-weight:bold;font-size:17px;font-family:'Helvetica Regular'"
							x="25"
							y="225"
							id="text46-6">
							<xsl:choose>
								<xsl:when
									test="$display/protein/@changeType = 'BETTER'">
									<xsl:attribute
										name="class">nut_main red</xsl:attribute>
								</xsl:when>
								<xsl:when
									test="$display/protein/@changeType = 'WORSE'">
									<xsl:attribute
										name="class">nut_main green</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:attribute
										name="class">nut_main</xsl:attribute>
								</xsl:otherwise>
							</xsl:choose>
           Protein: <xsl:value-of
								select="$protein"/>g</text>
						<text
							x="280"
							y="225"
							id="text48-14">
							<xsl:value-of
								select="$proteinRDA"/>%</text>
					</g>
				</g>
			</g>
		</svg>
	</xsl:template>


</xsl:stylesheet>
