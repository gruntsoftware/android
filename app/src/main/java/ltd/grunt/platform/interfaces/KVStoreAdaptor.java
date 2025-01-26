package ltd.grunt.platform.interfaces;

import ltd.grunt.platform.kvstore.CompletionObject;

public interface KVStoreAdaptor {

    public CompletionObject ver(String key);

    public CompletionObject put(String key, byte[] value, long version);

    public CompletionObject del(String key, long version);

    public CompletionObject get(String key, long version);

    public CompletionObject keys();

}
