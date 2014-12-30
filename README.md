##[CronusMaster](http://cronuspaas.github.io): CronusPaaS service controller [CronusAgent](http://cronuspaas.github.io): CronusPaaS agent
======

###Setup and Dev Instructions

#### In Production integrated deployed through Cronus Agent

* Install from web
* cd ~; wget -qO- 'http://cronuspaas.github.io/downloads/install_cronusmaster' | bash

#### In Dev Directly on Windows/Linux With Zero Installation: 
* Clone from Git
* Assuming have Java (JDK or most time just JRE) pre-installed.
* Note that for Linux/Mac user: need to chmod +x for play-1.2.4/play
* Run play run ../AgentMaster

#### Run/Debug in Eclipse:
* Clone from Git
* Extract to a folder, in command line run: ~/CronusMaster/play-1.2.7/play eclipsify CronusMaster
	* Note that for Linux/Mac user: need to chmod +x for play-1.2.7/play
* Import existing project in Eclipse: import the CronusMaster folder.
* Compile errors? Try rebuild and clean: (menu: Project->Clean->Clean all projects
* Run application: under "eclipse" folder: AgentMaster.launch : run as AgentMaster
* Then open browser: [localhost:9000](http://localhost:9000/)

#### Key configuraiton files
* application.conf : play settings
* actorconfig.conf : Akka settings
* routes : MVC settings as dispatcher

