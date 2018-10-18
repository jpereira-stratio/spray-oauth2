@Library('libpipelines@master') _

hose {
    MODULE = 'spray-oauth2'
    EMAIL = 'gosec'
    REPOSITORY = 'github.com/spray-oauth2'
    SHORTMODULE = 'so'

    SLACKTEAM = 'stratioSecurity'

    FOSS = true

    DEVTIMEOUT = 60
    RELEASETIMEOUT = 30
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
