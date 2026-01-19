# Lattice

A Simple (for now) Discord chat bridge for Hytale.

## Setup

### 1. Install The Mod

Put the jar file into the `mods` folder.

Then run the server with mod installed to generate the config file, it should generate it at `mods/TheClashFruit_Lattice/Lattice.json`.

The config file should look something like this:

```json
{
  "Enabled": false,
  "Discord": {
    "Token": "your_token_here",
    "ChannelId": "",
    "WebhookId": "",
    "Messages": {
      "Join": "%s joined %s.",
      "Leave": "%s left."
    }
  },
  "ChatPrefix": "[Discord]",
  "ChatPrefixColour": "#5865F2"
}
```

### 2. Create a Discord Bot

Create your bot on the discord developer portal and also enable `Message Content Intent` for your bot then paste the token into the config where it says `your_token_here`.

Don't forget to invite your bot, here is a template for the invite link: `https://discord.com/oauth2/authorize?client_id={client_id}&permissions=536938496&scope=bot+applications.commands`; Just replace `{client_id}` with your bot's client id.

### 3. Create a Channel & Webhook

Create your channel and copy its id into the channel id in the config. Then go into the settings for the channel and create a webhook and copy it's url, which should look something like `https://discord.com/api/webhooks/1460971511903948851/ydte1zUhNKBVzvpUlsAyGDrdAb83HvYRIl_KiDYhgKJ1x4kgMNCfMfAl3TGklrsegGmo`[^1], copy it's numerical part, here it's `1460971511903948851` and paste it into the config for the `WebhookId`.

### 4. Restart The Server

Set `Enabled` to true and restart the server and chat with people on Discord :3

(I suck at writing docs)

[^1]: No that webhook is deleted and I also mangled the token a bit :3