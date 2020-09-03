package com.bdf.rabbitId;

import com.bdf.rabbitId.exception.IdGeneratorException;
import com.bdf.rabbitId.model.IdStore;
import com.bdf.rabbitId.model.SegmentBuffer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * @author YangGuodong
 */
@Slf4j
public class BufferAllocatorFactory {
    private BufferPaddingExecutor bufferPaddingExecutor;
    private FilePersistenceExecutor filePersistenceExecutor;
    private boolean isPersistenceFile;
    private List<SegmentBuffer> segmentBuffers;

    private LoadState stage = LoadState.init;

    public BufferAllocatorFactory(BufferPaddingExecutor bufferPaddingExecutor, FilePersistenceExecutor filePersistenceExecutor){
        this.bufferPaddingExecutor = bufferPaddingExecutor;
        this.filePersistenceExecutor = filePersistenceExecutor;
        this.isPersistenceFile = true;
        if (isPersistenceFile) {
            segmentBuffers = filePersistenceExecutor.get();
        }
    }

    public BufferAllocator build(IdStore store) throws IdGeneratorException {
        this.stage = LoadState.init;
        return load(store);
    }

    private BufferAllocator load(IdStore store) throws IdGeneratorException {
        BufferAllocator allocator = null;
        switch (this.stage) {
            case init:
                nextState(LoadState.local);
                break;
            case local:
                allocator = executor(() -> {
                    if(null != segmentBuffers && segmentBuffers.size() > 0) {
                        Optional<SegmentBuffer> optional = segmentBuffers.stream().filter(ba -> ba.getKey().equals(store.getKey())).findFirst();
                        return optional.isPresent() ? BufferAllocator.build(optional.get(), bufferPaddingExecutor) : null;
                    }
                    return null;
                }, LoadState.register);
                break;
            case register:
                //从缓存中恢复，如果缓存不存在，则新注册
                allocator = executor(() -> BufferAllocator.build(store, bufferPaddingExecutor), LoadState.failure);
                break;
            default:
                log.error("initialize allocator error! IdStore:{}", store);
                throw new IdGeneratorException("initialize allocator error");
        }
        return null == allocator ? load(store) : allocator;
    }

    /**
     * 状态流转到下一个
     * @param stage
     */
    private void nextState(LoadState stage) {
        this.stage = stage;
    }

    private BufferAllocator executor(Loader loader, LoadState state){
        nextState(state);
        return loader.load();
    }

    interface Loader {
        /**
         *  装载BufferAllocator
         * @return
         */
        BufferAllocator load();
    }


    enum LoadState {
        init, local, register, failure
    }
}
