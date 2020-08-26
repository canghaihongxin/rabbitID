package com.bdf.rabbitId;

import com.alibaba.fastjson.JSON;
import com.bdf.rabbitId.model.SegmentBuffer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author YangGuodong
 */

@Slf4j
public class FilePersistenceExecutor {

    private final static String PERSISTENCE_FILE = "zebra.bup";

    private File dataDir;


    public FilePersistenceExecutor() {
        this(System.getProperty("user.dir"));
    }

    public FilePersistenceExecutor(String directory) {
        dataDir = new File(directory);
    }


    /**
     * 保存当前ID生成器镜像副本，以便下次启动进行恢复
     * @param allocatorMap
     * @throws Exception
     */
    public void put(Map<String, BufferAllocator> allocatorMap) throws Exception {
        log.info(System.getProperty("user.dir"));
        checkIsOpen();
        File file = new File(dataDir, String.format(PERSISTENCE_FILE));
        File backupFile = new File(dataDir,String.format(PERSISTENCE_FILE) + ".bak");
        if(file.exists()){
            boolean result = file.renameTo(backupFile);
            if(!result){
                backupFile.delete();
                file.renameTo(backupFile);
            }
        }
        try{
            FileOutputStream fos = new FileOutputStream(file);
            for (Map.Entry<String, BufferAllocator> entry: allocatorMap.entrySet()){
                fos.write((JSON.toJSONString(entry.getValue().getSegmentBuffer()) + "\n").getBytes());
            }
            fos.getFD().sync();
            fos.close();
            if (backupFile.exists()) {
                // The write has completed successfully, delete the backup
                backupFile.delete();
            }
        }finally {
            if (backupFile.exists()) {
                // The write has failed - restore the backup
                boolean result = backupFile.renameTo(file);
                if (!result) {
                    file.delete();
                    backupFile.renameTo(file);
                }
            }
            log.info("write segmentBuffers into {} success!", file.getName());
        }
    }


    public List<SegmentBuffer> get(){
        List<SegmentBuffer> segmentBuffers = new ArrayList<>();
        try {
            File file = new File(dataDir, String.format(PERSISTENCE_FILE));
            FileInputStream fis = new FileInputStream(file);
            BufferedReader buf = new BufferedReader(new InputStreamReader(fis));
            String line;
            SegmentBuffer segmentBuffer;
            while ((line = buf.readLine()) != null) {
                segmentBuffer = JSON.parseObject(line, SegmentBuffer.class);
                segmentBuffers.add(segmentBuffer);
            }
            //从本地文件获取buffer副本后，删除文件，防止下次因某种
            if(null != segmentBuffers && segmentBuffers.size() > 0){
                file.delete();
            }
            fis.close();
            buf.close();
        }
        catch(IOException ex) {

        }
        return segmentBuffers;
    }

    private void checkIsOpen() throws Exception {
//        if (clientFile == null) {
//            throw new Exception();
//        }
    }
}
