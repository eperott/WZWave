/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.frame;

import com.whizzosoftware.wzwave.channel.ZWaveChannelContext;
import com.whizzosoftware.wzwave.frame.transaction.DataFrameTransaction;
import com.whizzosoftware.wzwave.frame.transaction.RequestCallbackTransaction;
import com.whizzosoftware.wzwave.node.NodeInfo;
import io.netty.buffer.ByteBuf;

/**
 * A frame that controls the Z-Wave controller's "exclusion" mode.
 *
 * @author Dan Noguerol
 */
public class RemoveNodeFromNetwork extends DataFrame {
    public static final byte ID = 0x4B;

    /* Mode values */
    public static final byte REMOVE_NODE_ANY = 0x01;
    public static final byte REMOVE_NODE_CONTROLLER = 0x02;
    public static final byte REMOVE_NODE_SLAVE = 0x03;
    public static final byte REMOVE_NODE_STOP = 0x05;

    /* Status responses */
    public static final byte REMOVE_NODE_STATUS_LEARN_READY = 0x01;
    public static final byte REMOVE_NODE_STATUS_NODE_FOUND = 0x02;
    public static final byte REMOVE_NODE_STATUS_REMOVING_SLAVE = 0x03;
    public static final byte REMOVE_NODE_STATUS_REMOVING_CONTROLLER = 0x04;
    public static final byte REMOVE_NODE_STATUS_DONE = 0x06;
    public static final byte REMOVE_NODE_STATUS_FAILED = 0x07;

    private byte funcId;
    private byte status;
    private byte source;
    private NodeInfo nodeInfo;

    private static byte nextCallbackId;

    public RemoveNodeFromNetwork(byte mode) {
        super(DataFrameType.REQUEST, ID, new byte[] {mode, ++nextCallbackId});
    }

    public RemoveNodeFromNetwork(ByteBuf buffer) {
        super(buffer);

        funcId = buffer.readByte();
        status = buffer.readByte();
        source = buffer.readByte();

        // read node info if present
        byte len = buffer.readByte();
        if (len > 0) {
            byte basicClass = buffer.readByte();
            byte genericClass = buffer.readByte();
            byte specificClass = buffer.readByte();
            byte[] commandClasses = new byte[len-3];
            for (int i=0; i < len-3; i++) {
                commandClasses[i] = buffer.readByte();
            }
            nodeInfo = new NodeInfo(source, basicClass, genericClass, specificClass, commandClasses);
        }
    }

    public byte getStatus() {
        return status;
    }

    public byte getSource() {
        return source;
    }

    public boolean hasNodeInfo() {
        return (nodeInfo != null);
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    @Override
    public DataFrameTransaction createTransaction(ZWaveChannelContext ctx, boolean listeningNode) {
        return new RequestCallbackTransaction(ctx, this, listeningNode);
    }
}
