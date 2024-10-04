# PluginUpdater
PluginUpdater is a simple plugin and shadeable API that handles checking versions and automatically downloading plugin files from Modrinth, Spigot, Hangar and GitHub Releases. All updates and checks are ran asynchronously at a rate of 1 check/download per second to respect rate limits.

By default PluginUpdater comes with support for 50+ commonly used plugins, you can easily configure any other plugins you use that are hosted on supported sites.

## Commonly Asked Questions
**What if there is a major update to a plugin?**
- Plugins with major version updates _(according to [semver](https://semver.org/))_ will appear red in `/updates` and will not be updated when running `/updater update all`, this is to encourage server admins to take time to read changelogs. You can bypass this by either running `/updater update all -f` or `/updater update <plugin>`

**X plugin broke when I updated!**
- PluginUpdater only downloads plugins to the update folder and **does not** modify any plugin jars. If you have an issue with an update, report this to the developer of the plugin with the issue!

**Is there an API?**
- Adding PluginUpdater compatibility to your plugin is super easy and does not require an API. You can follow [these instructions](https://github.com/OakLoaf/PluginUpdater/wiki/PluginUpdater-Plugin#adding-support-for-pluginupdater) to make your plugin compatible!
- You can use our shadeable API to easily handle version checking/update downloading in your plugins. For more information about the API check out the [wiki page](https://github.com/OakLoaf/PluginUpdater/wiki/Shadeable-API)

<br>

## Getting support
If you need help setting up the plugin or have any questions, feel free to join the [LushPlugins discord server](https://discord.gg/mbPxvAxP3m)
