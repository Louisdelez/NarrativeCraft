/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fr.loudo.narrativecraft.network;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.scene.data.CameraAngle;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record S2CSyncCameraAnglesPacket(int chapterIndex, String sceneName, List<CameraAngle> cameraAngles)
        implements CustomPacketPayload {

    public static final Type<S2CSyncCameraAnglesPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "nc_sync_camera_angles"));

    public static final StreamCodec<ByteBuf, CameraAngle> CAMERA_ANGLE_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CameraAngle::getName,
            ByteBufCodecs.STRING_UTF8,
            CameraAngle::getDescription,
            CameraAngle::new);

    public static final StreamCodec<ByteBuf, S2CSyncCameraAnglesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            S2CSyncCameraAnglesPacket::chapterIndex,
            ByteBufCodecs.STRING_UTF8,
            S2CSyncCameraAnglesPacket::sceneName,
            CAMERA_ANGLE_STREAM_CODEC.apply(ByteBufCodecs.list()),
            S2CSyncCameraAnglesPacket::cameraAngles,
            S2CSyncCameraAnglesPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
