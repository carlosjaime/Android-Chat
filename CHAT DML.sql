------------------------------------------------------------------
-- CONSULTAS PARA RECIBIR MENSAJES.
-- NOTA: HAY QUE ADAPTARLO PARA QUE FUNCIONE EN PHP O DONDE SEA.
------------------------------------------------------------------

--------------------- PARTE 1. -------------

-- ¿POR QUE DIVIDIRLO EN 2 Y NO SACAR EL MAXIMO DIRECTAMENTE DE LA TABLA MENSAJES?
-- PORQUE SE PUEDE DAR EL CASO DE QUE EL SEÑOR X SUBE UN MESAJE Y EN ESE MISMO INSTANTE
-- VOY A RECIBIR LOS MENSAJES Y SACO EL MAX DE LOS MENSAJES, Y EL SEÑOR X NO HAYA 
-- ENLAZADO AUN LA TABLA ENVIAR MENSAJE CON LA TABLA MENSAJES, POR LO QUE ESE MENSAJE 
-- NO LO OBTENDRE NUNCA, YA QUE A LA HORA DE HACER LA CONSULTA ME SALDRA QUE ESE MENSAJE
-- NO ES PARA MI, Y LA SIGUIENTE VEZ QUE VAYA A LEER LOS MENSAJES PARTIRE COMO QUE ESE
-- YA ESTA LEIDO. SI LO HAGO DE ESTA FORMA OBTENDRE EL MAX DE LOS MENSAJES QUE YA ESTAN
-- ENLAZADOS.
 
INT I = SELECT MAX(SEND_INDIVIDUAL_MESSAGE.ID_MESSAGE) AS MAX_INDIVIDUAL
	FROM SEND_INDIVIDUAL_MESSAGE;
	
INT G = SELECT MAX(SEND_MESSAGE_GROUP.ID_MESSAGE) AS MAX_GROUP
	FROM SEND_MESSAGE_GROUP;

--------------------- PARTE 2. -------------

-- SI LA ID GRUPO ES 0, ES QUE ES UN MENSAJE INDIVIDUAL, SI NO, ES DE UN GRUPO
SELECT ID_USER_SENDER , MESSAGES.TEXT , 0 AS ID_GROUP
FROM USERS , MESSAGES , SEND_INDIVIDUAL_MESSAGE
WHERE USERS.ID_USER = SEND_INDIVIDUAL_MESSAGE.ID_USER_RECEIVER AND
	SEND_INDIVIDUAL_MESSAGE.ID_MESSAGE = MESSAGES.ID_MESSAGE AND
    USERS.ID_USER = ###MI ID### AND 
	MESSAGES.ID_MESSAGE > USERS.LAST_RECEIVED_MESSAGE_INDIVIDUAL
	AND MESSAGES.ID_MESSAGE <= ### I ###
UNION 
SELECT USERS.ID_USER AS ID_USER_SENDER, MESSAGES.TEXT , BELONG.ID_GROUP
FROM USERS , MESSAGES , SEND_MESSAGE_GROUP , BELONG
WHERE USERS.ID_USER = BELONG.ID_USER AND BELONG.ID_GROUP = SEND_MESSAGE_GROUP.ID_GROUP
	AND SEND_MESSAGE_GROUP.ID_MESSAGE = MESSAGES.ID_MESSAGE AND
    USERS.ID_USER = ###MI ID### AND
	MESSAGES.ID_MESSAGE > USERS.LAST_RECEIVED_MESSAGE_GROUP
	AND MESSAGES.ID_MESSAGE <= ### G ### ;
	
--------------------- PARTE 3. -------------
UPDATE USERS
	SET USERS.LAST_RECEIVED_MESSAGE_INDIVIDUAL = ### I ### , 
		USERS.LAST_RECEIVED_MESSAGE_GROUP = ### G ###
	WHERE USERS.ID_USER = ###MI ID### ;
	
	
------------------------------------------------------------------
-- CONSULTAS PARA ENVIAR MENSAJES.
-- NOTA: HAY QUE ADAPTARLO PARA QUE FUNCIONE EN PHP O DONDE SEA.
------------------------------------------------------------------

------------- MENSAJES INDIVIDUALES. ---------
------------- PARTE 1. ---------

-- IMPORTANTE: AL HACER ESTA INSERT HAY QUE UN RETURNING QUE TE DEVUELVA LA ID_MESSAGE
INSERT INTO MESSAGES ( TEXT , DATE_MESSAGE )
	VALUES ( ### EL CONTENIDO DEL MENSAJE ### , sysdate() ); 
	
------------- PARTE 2. ---------	

INSERT INTO  SEND_INDIVIDUAL_MESSAGE (ID_MESSAGE , ID_USER_SENDER , ID_USER_RECEIVER)
   VALUES (### ID_MESSAGE RECOGIDA ### , ### MI ID ### , ### ID DESTINO ###)

   
------------- MENSAJES A GRUPOS. ---------
------------- PARTE 1. ---------

-- IMPORTANTE: AL HACER ESTA INSERT HAY QUE UN RETURNING QUE TE DEVUELVA LA ID_MESSAGE
INSERT INTO MESSAGES ( TEXT , DATE_MESSAGE )
	VALUES ( ### EL CONTENIDO DEL MENSAJE ### , sysdate() ); 
	
------------- PARTE 2. ---------	

INSERT INTO  SEND_MESSAGE_GROUP (ID_MESSAGE , ID_USER , ID_GROUP)
   VALUES (### ID_MESSAGE RECOGIDA ### , ### MI ID ### , ### ID DEL GRUPO ###);
   

------------------------------------------------------------------
-- CONSULTAS PARA CREAR USUARIOS.
-- NOTA: HAY QUE ADAPTARLO PARA QUE FUNCIONE EN PHP O DONDE SEA.
------------------------------------------------------------------

INSERT INTO  USERS  (NICK , PHONE , PASSWORD)
   VALUES (### NICK ### , ### PHONE ### , ### PASSWORD ###);