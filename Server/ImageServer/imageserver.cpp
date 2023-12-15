#include "imageserver.h"
#include <QDebug>
#include <cstring>

ImageServer::ImageServer(QObject *parent)
    : QObject(parent)
{
    if (!server.listen(QHostAddress::Any, PORT)) {
        qDebug() << "Server could not start.";
        return;
    }

    qDebug() << "Server started on port " << PORT;

    connect(&server, &QTcpServer::newConnection, this, &ImageServer::onNewConnection);
}

QFile* ImageServer::createFile()
{
    QFile* file = nullptr;
    QString fileName;
    while (true) {
        fileName = "screenshot.png";
        file = new QFile(fileName);
        if (file->open(QIODevice::WriteOnly)) {
            break;
        } else {
            delete file;
            file = nullptr;
            break;
        }
    }

    return file;
}

int ImageServer::getLen(char b, char c, char d, char e)
{
    int s1 = b & 0xff;
    int s2 = c & 0xff;
    int s3 = d & 0xff;
    int s4 = e & 0xff;

    return ((s1 << 24) + (s2 << 16) + (s3 << 8) + (s4 << 0));
}

QByteArray ImageServer::getDataLength(int length) {
    QByteArray blen(4, 0);
    blen[0] = (length >> 24) & 0xFF;
    blen[1] = (length >> 16) & 0xFF;
    blen[2] = (length >> 8) & 0xFF;
    blen[3] = length & 0xFF;
    return blen;
}

QByteArray ImageServer::objectToByte(char header, QByteArray data) {
    QByteArray result;
    int size = 1 + 4 + data.size();
    result.resize(size);
    result[0] = header;
    QByteArray dataLen = getDataLength(data.size());
    std::memcpy(result.data() + 1, dataLen.constData(), 4);
    std::memcpy(result.data() + 5, data.constData(), data.size());
    return result;
}

void ImageServer::onNewConnection()
{
    QTcpSocket* socket = server.nextPendingConnection();
    qDebug() << "Client connected.";

    connect(socket, &QTcpSocket::readyRead, this, &ImageServer::onDataReceived);

    connectedClients.append(socket);
}

void ImageServer::onDataReceived()
{
    QTcpSocket* socket = qobject_cast<QTcpSocket*>(sender());
    if (!socket)
        return;

    buf = socket->readAll();

    if (buf.isEmpty()) {
        qDebug() << "Buffer is empty.";
        return;
    }

    if (buf.at(0) == HEADER_TEXT) {
        int dataLen = getLen(buf[1], buf[2], buf[3], buf[4]);
        QByteArray strData = buf.mid(5, dataLen);
        QString msg = QString::fromUtf8(strData);
        qDebug() << msg;

        //send data at all clients
        QByteArray dataPacket = objectToByte(HEADER_TEXT, strData);
        for(QTcpSocket* connectedClient : connectedClients){
            connectedClient->write(dataPacket);
        }
    }
    else if (buf.at(0) == HEADER_IMAGE) {
        QFile* file = createFile();
        int dataLen = getLen(buf[1], buf[2], buf[3], buf[4]);
        int fileSize = buf.size() - 5;

        if (file->isOpen() && file->isWritable()) {
            file->write(buf.mid(5, fileSize));

            while (dataLen > fileSize) {
                if (socket->waitForReadyRead(-1)) {
                    buf = socket->readAll();
                    fileSize += buf.size();
                    file->write(buf);
                    qDebug() << "총 사이즈 : " << dataLen << " / 받은 사이즈 : " << fileSize;
                } else {
                    qDebug() << "Socket read error.";
                    break;
                }
            }

            file->flush();
            file->close();
            qDebug() << "파일 쓰기 완료: " << file->fileName();
            delete file;
        } else {
            qDebug() << "File open error.";
        }
    }
}
