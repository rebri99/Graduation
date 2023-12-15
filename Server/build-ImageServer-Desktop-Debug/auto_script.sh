directory_to_watch="/home/pi/makeEnv/Server/build-ImageServer-Desktop-Debug"
python_script="/home/pi/makeEnv/Server/build-ImageServer-Desktop-Debug/detect_from_image.py"
lock_file="/home/pi/makeEnv/Server/build-ImageServer-Desktop-Debug/script.lock"

# 락 파일이 존재하면 스크립트가 이미 실행 중이므로 종료
if [ -f "$lock_file" ]; then
    echo "Script is already running."
    exit
fi

while true; do
    inotifywait -e modify "$directory_to_watch"
    if [ -f "$directory_to_watch/screenshot.png" ]; then
        # 파일 변경 시 실행할 명령어
        touch "$lock_file"
        python3 "$python_script"
        rm -f "$lock_file"
    fi
done
