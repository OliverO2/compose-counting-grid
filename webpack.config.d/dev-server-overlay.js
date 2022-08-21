// Disable dev-server's overlay display for warnings
// https://webpack.js.org/configuration/dev-server/#overlay
config.devServer = Object.assign(
    {},
    config.devServer || {},
    {
        client: {
            overlay: {
                errors: true,
                warnings: false,
            },
        },
    }
)
