// WORKAROUND js-joda
// https://kotlinlang.slack.com/archives/C01F2HV7868/p1698763134178559?thread_ts=1698759778.446179&cid=C01F2HV7868
config.resolve ?? (config.resolve = {});
config.resolve.alias ?? (config.resolve.alias = {});
config.resolve.alias.skia = false;
