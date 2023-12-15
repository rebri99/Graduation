import tensorflow as tf
import cv2
import numpy as np
import socket

# 데이터를 바이트 배열로 변환
def objectToByte(header, data):
    header_bytes = bytes([header])
    data_len = len(data)
    data_len_bytes = data_len.to_bytes(4, byteorder='big')
    result = header_bytes + data_len_bytes + data
    return result

# 클래스 레이블 매핑 딕셔너리
class_mapping = {
    1: "can",
    2: "glass",
    3: "metal",
    4: "paper",
    5: "plastic",
    6: "trash"
}


# .pb 파일로부터 모델을 불러옵니다.
model_path = "/home/pi/tensorflow1/models/research/object_detection/my_model/saved_model"
model = tf.saved_model.load(model_path)

# 이미지를 로드합니다.
image_path = "/home/pi/makeEnv/Server/build-ImageServer-Desktop-Debug/screenshot.png"
image = cv2.imread(image_path)
image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
image = cv2.resize(image, (512, 512))  # 모델에 따라 입력 이미지 크기 조절

# 이미지를 모델에 입력으로 전달하고 객체 탐지 수행
input_image = np.expand_dims(image, axis=0)
detections = model(input_image)

# 탐지된 객체의 라벨 값을 추출
labels = detections['detection_classes'][0].numpy().astype(int)
scores = detections['detection_scores'][0].numpy()

# 정확도가 70% 이상인 객체만 추출, 라벨 값을 문자열로 변환
label_strings = [class_mapping[labels[i]] for i in range(len(labels)) if scores[i] >= 0.7]

# 라벨 문자열을 UTF-8 인코딩을 사용하여 바이트로 변환
label_bytes = [label_str.encode('utf-8') for label_str in label_strings]

# 데이터 생성 및 소켓 통신
data = b','.join(label_bytes)  # 모든 라벨 바이트를 하나의 바이트 배열로 합치기
header = 0x01  # 헤더 값

# 데이터를 바이트 배열로 변환
data_packet = objectToByte(header, data)

# 결과를 서버로 전송
server_address = '125.139.250.60'
server_port = 9999
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.connect((server_address, server_port))
server_socket.sendall(data_packet)
server_socket.close()