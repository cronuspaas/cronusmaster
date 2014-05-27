package resources.agent;

import org.lightj.task.asynchttp.AsyncHttpTask.HttpMethod;

public class AgentRequests {
	
	public class Service {
		public class Create {
			public String service;
			public HttpMethod method = HttpMethod.POST;
		}
		public class Delete {
			public String service;
			public HttpMethod method = HttpMethod.DELETE;
		}
		public class Get {
			public String service;
			public HttpMethod method = HttpMethod.GET;
		}
		public class List {
			public HttpMethod method = HttpMethod.GET;
		}
	}
	
	public class Manifest {
		public class Create {
			public String service;
			public String manifest;
			public String[] packages;
			public HttpMethod method = HttpMethod.POST;
		}
	}

}
