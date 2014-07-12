/*  

Copyright [2013-2014] eBay Software Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package jobs;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.lightj.RuntimeContext;
import org.lightj.example.dal.LocalDatabaseEnum;
import org.lightj.initialization.BaseModule;
import org.lightj.initialization.InitializationProcessor;
import org.lightj.session.FlowModule;
import org.lightj.task.TaskModule;
import org.lightj.util.SpringContextUtil;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import play.Play;

import resources.PlayAnnotationConfigApplicationContext;
import resources.PlayResourceLoader;
import resources.PlayClassPathBeanDefinitionScanner;
import models.ResourceConfigs;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import resources.UserDataProvider;
import resources.elasticsearch.EsResourceProvider;
import resources.utils.VarUtils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
/**
 * 
 * @author ypei
 *
 */
@OnApplicationStart
public class Bootstrap extends Job {

    public void doJob() {
        RuntimeContext.setClusterUuid("restcommander", "prod", "all", Long.toString(System.currentTimeMillis()));
    	
        // initialize spring bean registration
		final Config conf = ConfigFactory.load("actorconfig");
		
        AnnotationConfigApplicationContext resourcesCtx = new PlayAnnotationConfigApplicationContext("resources");
        resourcesCtx.setClassLoader(Play.classloader);
//        resourcesCtx.setResourceLoader(new PlayResourceLoader());
//        PlayClassPathBeanDefinitionScanner playScanner = new PlayClassPathBeanDefinitionScanner(resourcesCtx);
//        playScanner.scan("resources");
//        resourcesCtx.refresh();
        
        SpringContextUtil.registerContext("resources", resourcesCtx);
        
        // initialize flow and task modules
        AnnotationConfigApplicationContext flowCtx = new AnnotationConfigApplicationContext("flows");
        InitializationProcessor initializer = new InitializationProcessor(
                new BaseModule[] {
                        new TaskModule().setActorSystemConfig("restcommander", conf).getModule(),
                        new FlowModule().setDb(LocalDatabaseEnum.TESTMEMDB)
                                        .setSpringContext(flowCtx)
                                        .setExectuorService(Executors.newFixedThreadPool(5))
                                        .getModule()
                });
        initializer.initialize();
        
        // initialize local elastic search
        if (VarUtils.LOCAL_ES_ENABLED) {
            EsResourceProvider.getEmbeddedEsServer();
        }
        
        // initialize user data 
        try {
            UserDataProvider.reloadAllConfigs();
        } catch (IOException e) {
            play.Logger.error(e, "error load configs");
        }
    }    
}