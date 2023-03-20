url="$(echo "$APK_URL")"
url=$(echo ${url} | cut -d "\"" -f 2)
url=$(echo "${url//\"\r\n\"/$ln}")
url=$(echo "${url//'\r\n'/$ln}")
url=$(echo "${url//\\r\\n/$ln}")

if [[ ! -z "$url" && "$url" != " " && "$url" != "null" ]]; then
	printf "\nAPK url: $url"
	printf "\n\nSending message to Telegram channel…\n"

  message=$"*New update available! (${RELEASE_TAG})* 🚀${ln}${ln}*Changes:*${ln}${CHANGELOG}"
  btns=$"{\"inline_keyboard\":[[[{\"text\":\"Download APK\",\"url\":\"${url}\"}]]}"

  telegramUrl="https://api.telegram.org/bot${TEL_BOT_KEY}/sendMessage?chat_id=@EvilInsultGenerator&text=${message}&parse_mode=Markdown&reply_markup=${btns}"
  curl -g "${telegramUrl}"

	printf "\n\nFinished sending notifications\n"
else
	printf "\n\nSkipping notifications because no file was uploaded\n"
fi
