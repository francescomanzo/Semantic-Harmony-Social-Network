package it.unisa.semanticSocial;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    private String profileKey;
    private PeerAddress peerAddress;

    public User(String profileKey, PeerAddress peerAddress){
        this.profileKey  = profileKey;
        this.peerAddress = peerAddress;
    }

    public String getProfileKey() {
        return profileKey;
    }

    public PeerAddress getPeerAddress() {
        return peerAddress;
    }
}
