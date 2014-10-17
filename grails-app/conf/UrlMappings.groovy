class UrlMappings {

	static mappings = {
		
		// Nif Connector
		"/cn/nif/search" {
			controller = "nif"
			action = [GET: "search"]
		}
		
		"/cn/nif/textmine" {
			controller = "nif"
			action = [POST: "textmine"]
		}
		
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
	}
	
}
