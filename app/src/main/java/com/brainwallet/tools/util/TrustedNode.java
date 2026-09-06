package com.brainwallet.tools.util;

public class TrustedNode {
    /**
     * Litecoin mainnet's standard P2P port (see {@code BRChainParams.h}'s {@code standardPort}).
     * {@code BRPeerManager.setFixedPeer()}'s native side already falls back to this when given
     * port 0, but that fallback was invisible to the user: an address saved without a port
     * displayed as just the host, with the actual port it connects on left unstated. Callers
     * that persist/display a trusted-node address should fill in this default explicitly via
     * {@link #withPort(String, int)} instead of relying on the native fallback.
     */
    public static final int STANDARD_PORT = 9333;

    /**
     * Combines a host and a port into the canonical "host:port" form this app persists and
     * displays, defaulting to {@link #STANDARD_PORT} when no explicit port (<= 0) is given.
     */
    public static String withPort(String host, int port) {
        int effectivePort = port > 0 ? port : STANDARD_PORT;
        return host + ":" + effectivePort;
    }

    public static  String getNodeHost(String input) {
        if (input.contains(":")) {
            return input.split(":")[0];
        }
        return input;
    }

    public static  int getNodePort(String input) {
        int port = 0;
        if (input.contains(":")) {
            try {
                port = Integer.parseInt(input.split(":")[1]);
            } catch (Exception e) {

            }
        }
        return port;
    }

    public static  boolean isValid(String input) {
        try {
            if (input == null || input.length() == 0) return false;
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (!Character.isDigit(c) && c != '.' && c != ':') return false;
            }
            String host;
            if (input.contains(":")) {
                String[] pieces = input.split(":");
                if (pieces.length > 2) return false;
                host = pieces[0];
                int port = Integer.parseInt(pieces[1]); //just try to see if it's a number
            } else {
                host = input;
            }
            String[] nums = host.split("\\.");
            if (nums.length != 4) return false;
            for (int i = 0; i < nums.length; i++) {
                int slice = Integer.parseInt(nums[i]);
                if (slice < 0 || slice > 255) return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}