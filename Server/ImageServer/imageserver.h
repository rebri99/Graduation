#ifndef IMAGESERVER_H
#define IMAGESERVER_H

#include <QObject>
#include <QTcpServer>
#include <QTcpSocket>
#include <QFile>
#include <QByteArray>
#include <QDir>
#include <QDate>

class ImageServer : public QObject
{
    Q_OBJECT

public:
    ImageServer(QObject *parent = 0);

private:
    QTcpServer server;
    const quint16 PORT = 9999;
    const char HEADER_TEXT = 0x01;
    const char HEADER_IMAGE = 0x02;
    QByteArray buf;

    QFile* createFile();
    int getLen(char b, char c, char d, char e);

    //send data
    QByteArray getDataLength(int length);
    QByteArray objectToByte(char header, QByteArray data);
    QList<QTcpSocket*> connectedClients;

private slots:
    void onNewConnection();
    void onDataReceived();
};

#endif // IMAGESERVER_H
