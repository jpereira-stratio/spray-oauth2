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
        
        doCompile(config)
        
        doUT(config)

        doPackage(config)
    
        parallel(QC: {
            doStaticAnalysis(config)
        }, DEPLOY: {
            doDeploy(config)
        }, failFast: config.FAILFAST)   
    }
}
