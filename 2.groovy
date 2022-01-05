import groovy.sql.Sql
import org.apache.commons.io.FilenameUtils

import java.sql.SQLException


    void copyFile (File sourceFile , String targetPath)
            throws IOException
    {//позволяет скопировать файл с помощью выполнения команд в cmd
        def command = [
                "CMD",
                "/C",
                "XCOPY",
                "/i",
                "/y",
                sourceFile.canonicalPath,//путь к файлу в стринг
                FilenameUtils.separatorsToSystem(targetPath)// преобразует путь к файлу к виду path\\to\\some\\file
        ];
        def proc = command.execute();//выполняет cmd команды указанные в данном списке
        proc.waitFor();//при необходимости заставляет текущий поток ждать, пока процесс, представленный этим Process объектом, не завершится
        if ( proc.exitValue() != 0 ) throw new IOException("File copy error for " + sourceFile.canonicalPath + " exit code: " + proc.exitValue());//если процесс не завершился выбрасить ошибку IOException
    }

    int executeCmd(List command, long nM)
    {
        List script = [];
        script += command;

        def proc = script.execute();//выполнить cmd команды в скрипте
        def sout = new StringBuffer();
        def serr = new StringBuffer();
        proc.consumeProcessOutput(sout, serr);//Получает потоки вывода и ошибок от процесса и считывает их, чтобы предотвратить блокировку процесса из-за полного буфера вывода.
        if (nM == 0 || nM == null) {//таймаут для процесса, если равен нулю, то просто ждем пока он завершится
            proc.waitFor();
        } else {
            proc.waitForOrKill(nM);//подождать пока процесс завершится( в течении переданного времени) в противном случае убить его
        }

        if(sout.size()>0) println("OS StdOut:\n" + sout.toString()); sout.delete(0, sout.length());//если буфер сообщений больше нуля вывести сообщение в консоль
        if(serr.size()>0) println("OS StdErr:\n" + serr.toString()); serr.delete(0, serr.length());//если буфер сообщений об ошибке больше нуля вывести сообщение в консоль

        return proc.exitValue();
    }

    File extractPGPFile(File inFile, String targetPath)
    {
        def command ;
        String unPGPFileName = FilenameUtils.removeExtension(inFile.name)+ ".txt"//меняет расешерние имя файла и записывает в формате .txt
        command = [
                "gpg",
//...
                "--yes" ,//Считать, что ответ на большинство вопросов положителен.
                "--batch",//Переход в пакетный режим. Ничего не спрашивать, не допускать диалогов.
                "--output" ,//Записать вывод в файл
                FilenameUtils.separatorsToSystem(targetPath + "/" + unPGPFileName),//преобразовать путь к ОС на которой запускается программа
                "--decrypt",//расшифровать файл
                inFile.canonicalPath//путь к файлу
        ];

        int exitValue = executeCmd(command, 50000);//выполни команду с таймаутом 50000(в данном случае выполняется разшифровка pgp файла)
        def delInd = inFile.delete();//высвобождаем память

        return new File(unPGPFileName);//возвращаем новый, уже расшифрованный pgp файл
    }



    static void main(String[] args) {
        String pathEMLStore = "S:\\...\\";

        def command = [
                "CMD",
                "/C",
                "ZERAT",
                "S:\\...\\get_last_mail.ini"
        ];
        def sout = new StringBuffer();
        def serr = new StringBuffer();

        int result = 0;
        int i = 0;
        while (result == 0)
        {
            def proc = new ProcessBuilder(command).start();
            proc.consumeProcessOutput(sout, serr);
            proc.waitFor();
            if(sout.size()>0) println "---------- Command-line stdout: ----------\n" + sout.toString();
            if(serr.size()>0) println "---------- Command-line stderr: ----------\n" + serr.toString();
            result = proc.exitValue();
            if (result == 0) i++;
            sout.delete(0, sout.length());
            serr.delete(0, serr.length());
        }

        new File(pathEMLStore).listFiles().findAll{file-> FilenameUtils.wildcardMatch(file.name, "TDAT*.pgp")}.each{ f->
            def ftxt = extractPGPFile(f, pathEMLStore + "ATM")
        }//расшифровывает все файлы по пути pathEMLStore


        def config = new ConfigSlurper().parse(new File("S:\\...\\config.groovy").toURL())
        String TargetTdpId = config.QueueHandler.databaseConnection.TargetTdpId;
        String TargetUserName = config.QueueHandler.databaseConnection.TargetUserName;
        String TargetUserPassword = config.QueueHandler.databaseConnection.TargetUserPassword;
        String TargetWorkingDatabase = config.QueueHandler.databases.TargetWorkingDatabase;
        String InQueueTable = config.QueueHandler.tables.InQueueTable;
        String pathMailGrabberOutputFolder = config.QueueHandler.path.pathMailGrabberOutputFolder;
        //тянет данные из конфигов, парсит их и разбивает на переменные стринг, для удобства


        def sql = Sql.newInstance("jdbc:teradata://${TargetTdpId}/TMODE=TERA,CHARSET=UTF8", "${TargetUserName}", "${TargetUserPassword}", "com.teradata.jdbc.TeraDriver");
        //открывает новое соединения с базой данных, по данным полученным из конфигов
        new File(pathEMLStore).listFiles().findAll{file-> (FilenameUtils.getExtension(file.name) == "eml")}.each{ f->
            try{
                copyFile(f, pathMailGrabberOutputFolder);//копирует расшифрованные файлы в папку оутпута
                sql.execute(
                        "INSERT INTO ${TargetWorkingDatabase}.${InQueueTable} (INCOMING_CHANNEL_ID, CANONICAL_FILE_PATH ) VALUES(?,?)" ,
                        ["1", pathMailGrabberOutputFolder + "${f.name}"]
                )//добавляет в базу данных файлы
            }
            catch (SQLException e) { println "!!! ERROR: ${f.canonicalPath} !!! " + e.getMessage() }
            catch (IOException e) { println "!!! ERROR: ${f.canonicalPath} !!! " + e.getMessage() }
        }
        sql.close();
    }
    // программа перебирает все файлы pgp расположенные по пути
    // , указанной в переменной pathEMLStore, копирует их в папку оутпута, которая описана в переменной конфига
    // , а также заносит записи в базу данных о пути уже разшифрованных файлов

