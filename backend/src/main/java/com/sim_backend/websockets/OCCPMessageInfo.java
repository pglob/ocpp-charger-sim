package com.sim_backend.websockets;

public @interface OCCPMessageInfo {
    /**
     * The Message name.
     * @return The Current Message Name.
     */
    String messageName() default "";
}
