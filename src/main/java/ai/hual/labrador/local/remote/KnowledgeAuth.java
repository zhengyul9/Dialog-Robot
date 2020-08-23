package ai.hual.labrador.local.remote;

/**
 * Authentication configuration of knowledge graph.
 * Created by Dai Wentao on 2017/5/15.
 */

class KnowledgeAuth {

    private String host;
    private int port;
    private String username;
    private String password;
    private String realm;
    private String scheme;

    KnowledgeAuth(String host, int port, String username, String password, String realm, String scheme) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.realm = realm;
        this.scheme = scheme;
    }

    public boolean isValid() {
        return username != null;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRealm() {
        return realm;
    }

    public String getScheme() {
        return scheme;
    }

}
