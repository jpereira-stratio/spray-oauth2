@Library('libpipelines@master') _

hose {
    MODULE = 'spray-oauth2'
    EMAIL = 'gosec'
    REPOSITORY = 'github.com/spray-oauth2'
    SHORTMODULE = 'so'
    CROSSBUILD = ['scala-2.10', 'scala-2.11']

    SLACKTEAM = 'stratioSecurity'

    FOSS = true

    DEVTIMEOUT = 15
    RELEASETIMEOUT = 10
    DEV = { config ->        
        
        parallel((config.CROSSBUILD[0]): {
            doCompile(conf: config, crossbuild: config.CROSSBUILD[0])
            doUT(conf: config, crossbuild: config.CROSSBUILD[0])
            doPackage(conf: config, crossbuild: config.CROSSBUILD[0])
    
            parallel(QC: {
                doStaticAnalysis(conf: config, crossbuild: config.CROSSBUILD[0])
            }, DEPLOY: {
                doDeploy(conf: config, crossbuild: config.CROSSBUILD[0])
            }, failFast: config.FAILFAST)
            
        }, (config.CROSSBUILD[1]): {
            doCompile(conf: config, crossbuild: config.CROSSBUILD[1])
            doUT(conf: config, crossbuild: config.CROSSBUILD[1])
            doPackage(conf: config, crossbuild: config.CROSSBUILD[1])
    
            parallel(QC: {
                doStaticAnalysis(conf: config, crossbuild: config.CROSSBUILD[1])
            }, DEPLOY: {
                doDeploy(conf: config, crossbuild: config.CROSSBUILD[1])
            }, failFast: config.FAILFAST)
        }, failFast: config.FAILFAST)
    }
}
