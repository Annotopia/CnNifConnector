class UrlMappings {

	static mappings = {
		
		"/cn/nif/search" {
			controller = "nif"
			action = [GET: "search"]
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
