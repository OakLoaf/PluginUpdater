# PluginUpdater
PluginUpdater is a simple plugin to handle checking versions and automatically downloading plugin files from Modrinth, Spigot, Hangar and GitHub Releases. All updates and checks are ran asynchronously at a rate of 1 check/download per second.

By default PluginUpdater comes with support for 50+ commonly used plugins, you can easily configure any other plugins you use that are hosted on supported sites.

## Commonly Asked Questions
**What if there is a major update to a plugin?**
- Plugins with major version updates _(according to [semver](https://semver.org/))_ will appear red in `/updates` and will not be updated when running `/updater update all`, this is to encourage server admins to take time to read changelogs. You can bypass this by either running `/updater update all -f` or `/updater update <plugin>`

**X plugin broke when I updated!**
- PluginUpdater only downloads plugins to the update folder and **does not** modify any plugin jars. If you have an issue with an update, report this to the developer of the plugin with the issue!

**Is there an API?**
- Adding PluginUpdater support to your plugin is super easy and does not require an API. You can follow [these instructions](https://github.com/OakLoaf/PluginUpdater/blob/master/README.md#adding-support-for-pluginupdater) to make your plugin compatible!
- A shadeable updater and version checker will be made available in the future.

<br>

## Configuring PluginUpdater

Adding new plugins is really easy! Each plugin requires the plugin name to be defined (this is case sensitive) and also the platform. Each platform will require different information, examples for each platform are shown below.

### Modrinth
  
```yml
  PluginName:
    platform: modrinth
    # A project id is a plugin's identifier which can be found in the "Technical Information" section on the Modrinth plugin page, as shown here (The project slug can also be used)
    modrinth-project-id: "djC8I9ui"
    # Some projects post all beta versions on Modrinth - disable this setting if you want to receive updates for all versions including beta versions
    featured-only: true
```

<details><summary><b>Finding the Project ID</b></summary>
  <img src="https://i.imgur.com/bmD3jlx.png"/>
</details>

### Spigot

```yml
  PluginName:
    platform: spigot
    # A resource id is a plugin's identifier which can be found at the end of the page's url
    spigot-resource-id: 107545
```

<details><summary><b>Finding the Resource ID</b></summary>
  <img src="https://i.imgur.com/ONpNxMz.png"/>
</details>

### Hangar

```yml
  PluginName:
    platform: hangar
    # A project slug is a plugin's identifier which can be found at the end of the page's url
    hangar-project-slug: "FancyHolograms"
```

### GitHub Releases

```yml
  PluginName:
    platform: github
    # The GitHub repo should be formatted as 'Owner/Repo'
    github-repo: "OakLoaf/LushRewards"
```

<br>

## Adding support for PluginUpdater
Making your plugin appear in PluginUpdater is super easy, just add any of the following lines into your `plugin.yml` with the correct information for your plugin. For information regarding finding the ids, reference the [Configuring PluginUpdater](https://github.com/OakLoaf/PluginUpdater/blob/master/README.md#configuring-pluginupdater) section.

```yml
modrinth-project-id: "djC8I9ui"
spigot-resource-id: 107545
hangar-project-slug: "FancyHolograms"
github-repo: "OakLoaf/LushRewards"
```

## Getting support
If you need help setting up the plugin or have any questions, feel free to join the [LushPlugins discord server](https://discord.gg/mbPxvAxP3m)
