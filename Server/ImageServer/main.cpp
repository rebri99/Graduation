#include "imageserver.h"
#include <QApplication>

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);

    ImageServer server;

    return a.exec();
}
