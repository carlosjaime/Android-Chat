#! /usr/bin/python3

__author__ = 'JoseAntonio'

import pymysql
from User import  User
from Message import  Message


class DataBase():

    def get_users(self):
        connection = pymysql.connect(host='146.185.155.88', port=3306, user='androiduser', passwd='12345', db='androidchat')
        cursor = connection.cursor()
        cursor.execute("SELECT * FROM USERS")

        users = list()

        for row in cursor:
            user = User()
            user.ID = row[0]
            user.NICK = row[1]
            user.STATUS = row[2]
            user.PHONE = row[3]
            user.USER_IMAGE = row[4]
            user.LAST_RECEIVED_MESSAGE = row[5]

            users.append(user.serialize())

        cursor.close()
        connection.close()
        return users

    def get_user(self, ID):
        connection = pymysql.connect(host='146.185.155.88', port=3306, user='androiduser', passwd='12345', db='androidchat')
        cursor = connection.cursor()
        cursor.execute("SELECT * FROM USERS WHERE ID_USER = " + str(ID))

        user = User()
        user.ID = cursor._rows[0][0]
        user.NICK = cursor._rows[0][1]
        user.STATUS = cursor._rows[0][2]
        user.PHONE = cursor._rows[0][3]
        user.USER_IMAGE = cursor._rows[0][4]
        user.LAST_RECEIVED_MESSAGE = cursor._rows[0][5]

        cursor.close()
        connection.close()
        return user.serialize()

    def get_messages(self, ID):
        connection = pymysql.connect(host='146.185.155.88', port=3306, user='androiduser', passwd='12345', db='androidchat')
        cursor = connection.cursor()

        #Conseguir último mensaje individual.
        SQL = """SELECT MAX(SEND_INDIVIDUAL_MESSAGE.ID_MESSAGE) AS MAX_INDIVIDUAL
                 FROM SEND_INDIVIDUAL_MESSAGE""".replace('\n',' ')

        cursor.execute(SQL)
        maxI = cursor._rows[0][0]

        #Conseguir último mensaje grupal.
        SQL = """SELECT MAX(SEND_MESSAGE_GROUP.ID_MESSAGE) AS MAX_GROUP
                FROM SEND_MESSAGE_GROUP""".replace('\n',' ')

        cursor.execute(SQL)
        maxG = cursor._rows[0][0]

        if maxG is None:
            maxG = 0

        if maxI is None:
            maxI = 0

        #Recibir mensajes
        SQL = """SELECT ID_USER_SENDER , MESSAGES.TEXT , 0 AS ID_GROUP
                 FROM USERS , MESSAGES , SEND_INDIVIDUAL_MESSAGE
                 WHERE USERS.ID_USER = SEND_INDIVIDUAL_MESSAGE.ID_USER_RECEIVER
                       AND SEND_INDIVIDUAL_MESSAGE.ID_MESSAGE = MESSAGES.ID_MESSAGE
                       AND USERS.ID_USER = %s
                       AND MESSAGES.ID_MESSAGE > USERS.LAST_RECEIVED_MESSAGE_INDIVIDUAL
                       AND MESSAGES.ID_MESSAGE <= %s
                UNION
                SELECT USERS.ID_USER AS ID_USER_SENDER, MESSAGES.TEXT , BELONG.ID_GROUP
                FROM USERS , MESSAGES , SEND_MESSAGE_GROUP , BELONG
                WHERE USERS.ID_USER = BELONG.ID_USER AND BELONG.ID_GROUP = SEND_MESSAGE_GROUP.ID_GROUP
                      AND SEND_MESSAGE_GROUP.ID_MESSAGE = MESSAGES.ID_MESSAGE AND
                      USERS.ID_USER = %s AND
                      MESSAGES.ID_MESSAGE > USERS.LAST_RECEIVED_MESSAGE_GROUP
                      AND MESSAGES.ID_MESSAGE <= %s ;""".replace('\n',' ')

        SQL = SQL % (str(ID),str(maxI),str(ID),str(maxG))
        cursor.execute(SQL)

        messages = list()

        for row in cursor:
            message = Message()
            message.ID_USER_SENDER = row[0]
            message.TEXT = row[1]
            message.ID_GROUP = row[2]

            messages.append(message.serialize())

        #Actualizar ultimo mensaje.
        SQL = """UPDATE USERS
	             SET USERS.LAST_RECEIVED_MESSAGE_INDIVIDUAL = %s ,
		             USERS.LAST_RECEIVED_MESSAGE_GROUP = %s
	             WHERE USERS.ID_USER = %s ;""".replace('\n',' ').replace('\t','')

        SQL = SQL % (str(maxI),str(maxG),str(ID))
        cursor.execute(SQL)

        cursor.close()
        return messages

    def send_message(self, ID, message, idDest):
        connection = pymysql.connect(host='146.185.155.88', port=3306, user='androiduser', passwd='12345', db='androidchat')
        cursor = connection.cursor()

        SQL = """INSERT INTO MESSAGES ( TEXT , DATE_MESSAGE )
	              VALUES ('%s', sysdate() );
	              """.replace('\n',' ').replace('\t','')
        SQL = SQL % str(message)

        cursor.execute(SQL)
        connection.commit()
        messageID = cursor.lastrowid

        SQL = """INSERT INTO  SEND_INDIVIDUAL_MESSAGE
                 (ID_MESSAGE , ID_USER_RECEIVER, ID_USER_SENDER)
                 VALUES (%s , %s , %s);
	              """.replace('\n',' ')
        SQL = SQL % (str(messageID), str(ID), str(idDest))

        cursor.execute(SQL)
        connection.commit()
        connection.close()
        return messageID