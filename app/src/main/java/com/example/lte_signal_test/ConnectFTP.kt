package com.example.lte_signal_test

import android.content.ContentValues.TAG
import android.util.Log
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.lang.Exception
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTP
import java.io.FileInputStream
import java.io.FileOutputStream


class ConnectFTP {
    var mFTPClient : FTPClient? = null
    fun ConnectFTP() {
        mFTPClient = FTPClient()
    }
    fun ftpConnect(host: String?, username: String?, password: String?, port: Int): Boolean {
        var result = false
        if (mFTPClient != null) {
            try {
                mFTPClient!!.connect(host, port)
                if (FTPReply.isPositiveCompletion(mFTPClient!!.replyCode)) {
                    result = mFTPClient!!.login(username, password)
                    mFTPClient!!.bufferSize = 1024*1024
                    mFTPClient!!.enterLocalPassiveMode()
                }
            } catch (e: Exception) {
                Log.d(TAG, "Couldn't connect to host")
            }
        }
        return result
    }

    fun ftpDisconnect(): Boolean {
        var result = false
        try {
            mFTPClient!!.logout()
            mFTPClient!!.disconnect()
            result = true
        } catch (e: Exception) {
            Log.d(TAG, "Failed to disconnect with server")
        }
        return result
    }

    fun ftpGetDirectory(): String? {
        var directory: String? = null
        try {
            directory = mFTPClient!!.printWorkingDirectory()
        } catch (e: Exception) {
            Log.d(TAG, "Couldn't get current directory")
        }
        return directory
    }

    fun ftpChangeDirctory(directory: String?): Boolean {
        try {
            mFTPClient!!.changeWorkingDirectory(directory)
            return true
        } catch (e: Exception) {
            Log.d(TAG, "Couldn't change the directory")
        }
        return false
    }

    fun ftpGetFileList(directory: String?): Array<String?>? {
        var fileList: Array<String?>? = null
        var i = 0
        try {
            val ftpFiles = mFTPClient!!.listFiles(directory)
            fileList = arrayOfNulls(ftpFiles.size)
            for (file in ftpFiles) {
                val fileName = file.name
                if (file.isFile) {
                    fileList[i] = "(File) $fileName"
                } else {
                    fileList[i] = "(Directory) $fileName"
                }
                i++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return fileList
    }

    fun ftpCreateDirectory(directory: String?): Boolean {
        var result = false
        try {
            result = mFTPClient!!.makeDirectory(directory)
        } catch (e: Exception) {
            Log.d(TAG, "Couldn't make the directory")
        }
        return result
    }

    fun ftpDeleteDirectory(directory: String?): Boolean {
        var result = false
        try {
            result = mFTPClient!!.removeDirectory(directory)
        } catch (e: Exception) {
            Log.d(TAG, "Couldn't remove directory")
        }
        return result
    }

    fun ftpDeleteFile(file: String?): Boolean {
        var result = false
        try {
            result = mFTPClient!!.deleteFile(file)
        } catch (e: Exception) {
            Log.d(TAG, "Couldn't remove the file")
        }
        return result
    }

    fun ftpRenameFile(from: String?, to: String?): Boolean {
        var result = false
        try {
            result = mFTPClient!!.rename(from, to)
        } catch (e: Exception) {
            Log.d(TAG, "Couldn't rename file")
        }
        return result
    }

    fun ftpDownloadFile(srcFilePath: String?, desFilePath: String?): Boolean {
        var result = false
        try {
            mFTPClient!!.setFileType(FTP.BINARY_FILE_TYPE)
            mFTPClient!!.setFileTransferMode(FTP.BINARY_FILE_TYPE)
            val fos = FileOutputStream(desFilePath)
            result = mFTPClient!!.retrieveFile(srcFilePath, fos)
            fos.close()
        } catch (e: Exception) {
            Log.d(TAG, "Download failed")
        }
        return result
    }

    fun ftpUploadFile(srcFilePath: String?, desFileName: String?, desDirectory: String?): Boolean {
        var result = false
        try {
            val fis = FileInputStream(srcFilePath)
            if (ftpChangeDirctory(desDirectory)) {
                result = mFTPClient!!.storeFile(desFileName, fis)
            }
            fis.close()
        } catch (e: Exception) {
            Log.d(TAG, "Couldn't upload the file")
        }
        return result
    }
}