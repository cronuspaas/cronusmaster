[Cronus Master](http://www.restcommander.com): centralized service used to control [Cronus Agent](http://) and perform other http tasks in scale![Travis status](https://api.travis-ci.org/eBay/restcommander.png?branch=master)
===========

###Background
Root from [Rest Commander](http://www.restcommander.com) 

###Setup and Dev Instructions

#### In Production integrated deployed through Cronus Agent

* Install [cronusagent](https://github.com/yubin154/cronusagent)
* Pull source from Git
* Run ./install.sh 
* Service is available at http://localhost:9000

#### In Dev Directly on Windows/Linux With Zero Installation: 
* Assuming have Java (JDK or most time just JRE) pre-installed.
* Note that for Linux/Mac user: need to chmod +x for play-1.2.4/play
* Run play run ../AgentMaster

#### Run/Debug in Eclipse:
* Clone project in Git from: https://github.com/eBay/restcommander
* Extract to a folder, e.g., S:\GitSources\AgentMaster\AgentMaster. In command line run: S:\GitSources\AgentMaster\AgentMaster>play-1.2.4\play eclipsify AgentMaster
	* Note that for Linux/Mac user: need to chmod +x for play-1.2.4/play
* Import existing project in Eclipse: import the AgentMaster folder.
* Compile errors? Try rebuild and clean: (menu: Project->Clean->Clean all projects
* Run application: under "eclipse" folder: AgentMaster.launch : run as AgentMaster
* Then open browser: [localhost:9000](http://localhost:9000/)

#### Key configuraiton files
* application.conf : play settings
* actorconfig.conf : Akka settings
* routes : MVC settings as dispatcher

