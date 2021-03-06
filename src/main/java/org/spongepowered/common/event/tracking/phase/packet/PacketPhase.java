/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.event.tracking.phase.packet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketEnchantItem;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlaceRecipe;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketSpectate;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.packet.drag.DragInventoryAddSlotState;
import org.spongepowered.common.event.tracking.phase.packet.drag.DragInventoryStartState;
import org.spongepowered.common.event.tracking.phase.packet.drag.MiddleDragInventoryStopState;
import org.spongepowered.common.event.tracking.phase.packet.drag.PrimaryDragInventoryStopState;
import org.spongepowered.common.event.tracking.phase.packet.drag.SecondaryDragInventoryStopState;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public final class PacketPhase extends TrackingPhase {


    public static final class General {

        public static final IPhaseState<BasicPacketContext> UNKNOWN = new UnknownPacketState();
        public static final IPhaseState<BasicPacketContext> MOVEMENT = new MovementPacketState();
        public static final IPhaseState<BasicPacketContext> INTERACTION = new InteractionPacketState();
        public static final IPhaseState<BasicPacketContext> IGNORED = new IgnoredPacketState();
        public static final IPhaseState<BasicPacketContext> INTERACT_ENTITY = new InteractEntityPacketState();
        public static final IPhaseState<BasicPacketContext> ATTACK_ENTITY = new AttackEntityPacketState();
        public static final IPhaseState<BasicPacketContext> INTERACT_AT_ENTITY = new InteractAtEntityPacketState();
        public static final IPhaseState<BasicPacketContext> CREATIVE_INVENTORY = new CreativeInventoryPacketState();
        public static final IPhaseState<BasicPacketContext> PLACE_BLOCK = new PlaceBlockPacketState();
        public static final IPhaseState<BasicPacketContext> REQUEST_RESPAWN = new BasicPacketState();
        public static final IPhaseState<BasicPacketContext> USE_ITEM = new UseItemPacketState();
        public static final IPhaseState<BasicPacketContext> INVALID = new InvalidPacketState();
        public static final IPhaseState<BasicPacketContext> START_RIDING_JUMP = new BasicPacketState();
        public static final IPhaseState<BasicPacketContext> ANIMATION = new AnimationPacketState();
        public static final IPhaseState<BasicPacketContext> START_SNEAKING = new BasicPacketState();
        public static final IPhaseState<BasicPacketContext> STOP_SNEAKING = new BasicPacketState();
        public static final IPhaseState<BasicPacketContext> START_SPRINTING = new BasicPacketState();
        public static final IPhaseState<BasicPacketContext> STOP_SPRINTING = new BasicPacketState();
        public static final IPhaseState<BasicPacketContext> STOP_SLEEPING = new StopSleepingPacketState();
        public static final IPhaseState<BasicPacketContext> CLOSE_WINDOW = new CloseWindowState();
        public static final IPhaseState<BasicPacketContext> UPDATE_SIGN = new UpdateSignState();
        public static final IPhaseState<BasicPacketContext> RESOURCE_PACK = new ResourcePackState();
        public static final IPhaseState<BasicPacketContext> STOP_RIDING_JUMP = new BasicPacketState();
        public static final IPhaseState<BasicPacketContext> HANDLED_EXTERNALLY = new UnknownPacketState();
        public static final IPhaseState<BasicPacketContext> START_FALL_FLYING = new BasicPacketState();
    }

    public static final class Inventory {
        public static final BasicInventoryPacketState INVENTORY = new BasicInventoryPacketState();
        public static final BasicInventoryPacketState PRIMARY_INVENTORY_CLICK = new PrimaryInventoryClickState();
        public static final BasicInventoryPacketState SECONDARY_INVENTORY_CLICK = new SecondaryInventoryClickState();
        public static final BasicInventoryPacketState MIDDLE_INVENTORY_CLICK = new MiddleInventoryClickState();
        public static final BasicInventoryPacketState DROP_ITEM_OUTSIDE_WINDOW = new DropItemOutsideWindowState();
        public static final BasicInventoryPacketState DROP_ITEM_WITH_HOTKEY = new DropItemWithHotkeyState();
        public static final BasicInventoryPacketState DROP_ITEMS = new BasicInventoryPacketState();
        public static final BasicInventoryPacketState DROP_INVENTORY = new DropInventoryState();
        public static final BasicInventoryPacketState SWITCH_HOTBAR_NUMBER_PRESS = new SwitchHotbarNumberPressState();
        public static final BasicInventoryPacketState PRIMARY_INVENTORY_SHIFT_CLICK = new PrimaryInventoryShiftClick();
        public static final BasicInventoryPacketState SECONDARY_INVENTORY_SHIFT_CLICK = new SecondaryInventoryShiftClickState();
        public static final BasicInventoryPacketState DOUBLE_CLICK_INVENTORY = new DoubleClickInventoryState();

        public static final BasicInventoryPacketState PRIMARY_DRAG_INVENTORY_START = new DragInventoryStartState("PRIMARY_DRAG_INVENTORY_START", DRAG_MODE_PRIMARY_BUTTON);
        public static final BasicInventoryPacketState SECONDARY_DRAG_INVENTORY_START = new DragInventoryStartState("SECONDARY_DRAG_INVENTORY_START", DRAG_MODE_SECONDARY_BUTTON);
        public static final BasicInventoryPacketState MIDDLE_DRAG_INVENTORY_START = new DragInventoryStartState("MIDDLE_DRAG_INVENTORY_START", DRAG_MODE_MIDDLE_BUTTON);

        public static final BasicInventoryPacketState PRIMARY_DRAG_INVENTORY_ADDSLOT = new DragInventoryAddSlotState("PRIMARY_DRAG_INVENTORY_ADD_SLOT", DRAG_MODE_PRIMARY_BUTTON);
        public static final BasicInventoryPacketState SECONDARY_DRAG_INVENTORY_ADDSLOT = new DragInventoryAddSlotState("SECONDARY_DRAG_INVENTORY_ADD_SLOT", DRAG_MODE_SECONDARY_BUTTON);
        public static final BasicInventoryPacketState MIDDLE_DRAG_INVENTORY_ADDSLOT = new DragInventoryAddSlotState("MIDDLE_DRAG_INVENTORY_ADD_SLOT", DRAG_MODE_MIDDLE_BUTTON);

        public static final BasicInventoryPacketState PRIMARY_DRAG_INVENTORY_STOP = new PrimaryDragInventoryStopState();
        public static final BasicInventoryPacketState SECONDARY_DRAG_INVENTORY_STOP = new SecondaryDragInventoryStopState();
        public static final BasicInventoryPacketState MIDDLE_DRAG_INVENTORY_STOP = new MiddleDragInventoryStopState();

        public static final BasicInventoryPacketState SWITCH_HOTBAR_SCROLL = new SwitchHotbarScrollState();
        public static final BasicInventoryPacketState OPEN_INVENTORY = new OpenInventoryState();
        public static final BasicInventoryPacketState ENCHANT_ITEM = new EnchantItemPacketState();
        public static final BasicInventoryPacketState SWAP_HAND_ITEMS = new SwapHandItemsState();

        public static final BasicInventoryPacketState PLACE_RECIPE = new PlaceRecipePacketState();

        static final ImmutableList<BasicInventoryPacketState> VALUES = ImmutableList.<BasicInventoryPacketState>builder()
                .add(INVENTORY)
                .add(PRIMARY_INVENTORY_CLICK)
                .add(SECONDARY_INVENTORY_CLICK)
                .add(MIDDLE_INVENTORY_CLICK)
                .add(DROP_ITEM_OUTSIDE_WINDOW)
                .add(DROP_ITEM_WITH_HOTKEY)
                .add(DROP_ITEMS)
                .add(DROP_INVENTORY)
                .add(SWITCH_HOTBAR_NUMBER_PRESS)
                .add(PRIMARY_INVENTORY_SHIFT_CLICK)
                .add(SECONDARY_INVENTORY_SHIFT_CLICK)
                .add(DOUBLE_CLICK_INVENTORY)

                .add(PRIMARY_DRAG_INVENTORY_START)
                .add(SECONDARY_DRAG_INVENTORY_START)
                .add(MIDDLE_DRAG_INVENTORY_START)

                .add(PRIMARY_DRAG_INVENTORY_ADDSLOT)
                .add(SECONDARY_DRAG_INVENTORY_ADDSLOT)
                .add(MIDDLE_DRAG_INVENTORY_ADDSLOT)

                .add(PRIMARY_DRAG_INVENTORY_STOP)
                .add(SECONDARY_DRAG_INVENTORY_STOP)
                .add(MIDDLE_DRAG_INVENTORY_STOP)

                .add(SWITCH_HOTBAR_SCROLL)
                .add(OPEN_INVENTORY)
                .add(ENCHANT_ITEM)
                .build();

    }

    // Inventory static fields
    public final static int MAGIC_CLICK_OUTSIDE_SURVIVAL = -999;
    public final static int MAGIC_CLICK_OUTSIDE_CREATIVE = -1;

    // Flag masks
    public final static int MASK_NONE              = 0x00000;
    public final static int MASK_OUTSIDE           = 0x30000;
    public final static int MASK_MODE              = 0x0FE00;
    public final static int MASK_DRAGDATA          = 0x001F8;
    public final static int MASK_BUTTON            = 0x00007;

    // Mask presets
    public final static int MASK_ALL               = MASK_OUTSIDE | MASK_MODE | MASK_BUTTON | MASK_DRAGDATA;
    public final static int MASK_NORMAL            = MASK_MODE | MASK_BUTTON | MASK_DRAGDATA;
    public final static int MASK_DRAG              = MASK_OUTSIDE | MASK_NORMAL;

    // Click location semaphore flags
    public final static int CLICK_INSIDE_WINDOW    = 0x01 << 16 << 0;
    public final static int CLICK_OUTSIDE_WINDOW   = 0x01 << 16 << 1;
    public final static int CLICK_ANYWHERE         = CLICK_INSIDE_WINDOW | CLICK_OUTSIDE_WINDOW;

    // Modes flags
    public final static int MODE_CLICK             = 0x01 << 9 << ClickType.PICKUP.ordinal();
    public final static int MODE_SHIFT_CLICK       = 0x01 << 9 << ClickType.QUICK_MOVE.ordinal();
    public final static int MODE_HOTBAR            = 0x01 << 9 << ClickType.SWAP.ordinal();
    public final static int MODE_PICKBLOCK         = 0x01 << 9 << ClickType.CLONE.ordinal();
    public final static int MODE_DROP              = 0x01 << 9 << ClickType.THROW.ordinal();
    public final static int MODE_DRAG              = 0x01 << 9 << ClickType.QUICK_CRAFT.ordinal();
    public final static int MODE_DOUBLE_CLICK      = 0x01 << 9 << ClickType.PICKUP_ALL.ordinal();

    // Drag mode flags, bitmasked from button and only set if MODE_DRAG
    public final static int DRAG_MODE_PRIMARY_BUTTON = 0x01 << 6 << 0;
    public final static int DRAG_MODE_SECONDARY_BUTTON = 0x01 << 6 << 1;
    public final static int DRAG_MODE_MIDDLE_BUTTON = 0x01 << 6 << 2;
    public final static int DRAG_MODE_ANY          = DRAG_MODE_PRIMARY_BUTTON | DRAG_MODE_SECONDARY_BUTTON | DRAG_MODE_MIDDLE_BUTTON;

    // Drag status flags, bitmasked from button and only set if MODE_DRAG
    public final static int DRAG_STATUS_STARTED    = 0x01 << 3 << 0;
    public final static int DRAG_STATUS_ADD_SLOT   = 0x01 << 3 << 1;
    public final static int DRAG_STATUS_STOPPED    = 0x01 << 3 << 2;

    // Buttons flags, only set if *not* MODE_DRAG
    public final static int BUTTON_PRIMARY         = 0x01 << 0 << 0;
    public final static int BUTTON_SECONDARY       = 0x01 << 0 << 1;
    public final static int BUTTON_MIDDLE          = 0x01 << 0 << 2;


    // Only use these with data from the actual packet. DO NOT
    // use them as enum constant values (the 'stateId')
    public final static int PACKET_BUTTON_PRIMARY_ID = 0;
    public final static int PACKET_BUTTON_SECONDARY_ID = 0;
    public final static int PACKET_BUTTON_MIDDLE_ID = 0;

    private final Map<Class<? extends Packet<?>>, Function<Packet<?>, IPhaseState<? extends PacketContext<?>>>> packetTranslationMap = new IdentityHashMap<>();

    // General use methods

    public boolean isPacketInvalid(Packet<?> packetIn, EntityPlayerMP packetPlayer, IPhaseState<? extends PacketContext<?>> packetState) {
        return ((PacketState<?>) packetState).isPacketIgnored(packetIn, packetPlayer);
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public IPhaseState<? extends PacketContext<?>> getStateForPacket(Packet<?> packet) {
        final Function<Packet<?>, IPhaseState<? extends PacketContext<?>>> packetStateFunction = this.packetTranslationMap.get(packet.getClass());
        if (packetStateFunction != null) {
            return packetStateFunction.apply(packet);
        }
        return PacketPhase.General.UNKNOWN;
    }

    public PhaseContext<?> populateContext(Packet<?> packet, EntityPlayerMP entityPlayerMP, IPhaseState<?> state, PhaseContext<?> context) {
        checkNotNull(packet, "Packet cannot be null!");
        checkArgument(!context.isComplete(), "PhaseContext cannot be marked as completed!");
        ((PacketState) state).populateContext(entityPlayerMP, packet, (PacketContext) context);
        return context;
    }

    // Inventory packet specific methods

    public static BasicInventoryPacketState fromWindowPacket(CPacketClickWindow windowPacket) {
        final int mode = 0x01 << 9 << windowPacket.getClickType().ordinal();
        final int packed = windowPacket.getUsedButton();
        final int unpacked = mode == MODE_DRAG ? (0x01 << 6 << (packed >> 2 & 3)) | (0x01 << 3 << (packed & 3)) : (0x01 << (packed & 3));

        BasicInventoryPacketState inventory = fromState(clickType(windowPacket.getSlotId()) | mode | unpacked);
        if (inventory == Inventory.INVENTORY) {
            SpongeImpl.getLogger().warn(String.format("Unable to find InventoryPacketState handler for click window packet: %s", windowPacket));
        }
        return inventory;
    }


    private static int clickType(int slotId) {
        return (slotId == MAGIC_CLICK_OUTSIDE_SURVIVAL || slotId == MAGIC_CLICK_OUTSIDE_CREATIVE) ? CLICK_OUTSIDE_WINDOW : CLICK_INSIDE_WINDOW;
    }


    public static BasicInventoryPacketState fromState(final int state) {
        for (BasicInventoryPacketState inventory : Inventory.VALUES) {
            if (inventory.matches(state)) {
                return inventory;
            }
        }
        return Inventory.INVENTORY;
    }

    // General methods

    public static PacketPhase getInstance() {
        return Holder.INSTANCE;
    }

    private PacketPhase() {
        setupPacketToStateMapping();
    }

    private static final class Holder {
        static final PacketPhase INSTANCE = new PacketPhase();
    }


    public void setupPacketToStateMapping() {
        this.packetTranslationMap.put(CPacketKeepAlive.class, packet -> General.IGNORED);
        this.packetTranslationMap.put(CPacketChatMessage.class, packet -> General.HANDLED_EXTERNALLY);
        this.packetTranslationMap.put(CPacketUseEntity.class, packet -> {
            final CPacketUseEntity useEntityPacket = (CPacketUseEntity) packet;
            final CPacketUseEntity.Action action = useEntityPacket.getAction();
            if (action == CPacketUseEntity.Action.INTERACT) {
                return General.INTERACT_ENTITY;
            } else if (action == CPacketUseEntity.Action.ATTACK) {
                return General.ATTACK_ENTITY;
            } else if (action == CPacketUseEntity.Action.INTERACT_AT) {
                return General.INTERACT_AT_ENTITY;
            } else {
                return General.INVALID;
            }
        });
        this.packetTranslationMap.put(CPacketPlayer.class, packet -> General.MOVEMENT);
        this.packetTranslationMap.put(CPacketPlayer.Position.class, packet -> General.MOVEMENT);
        this.packetTranslationMap.put(CPacketPlayer.Rotation.class, packet -> General.MOVEMENT);
        this.packetTranslationMap.put(CPacketPlayer.PositionRotation.class, packet -> General.MOVEMENT);
        this.packetTranslationMap.put(CPacketPlayerDigging.class, packet -> {
            final CPacketPlayerDigging playerDigging = (CPacketPlayerDigging) packet;
            final CPacketPlayerDigging.Action action = playerDigging.getAction();
            final IPhaseState<? extends PacketContext<?>> state = INTERACTION_ACTION_MAPPINGS.get(action);
            return state == null ? General.UNKNOWN : state;
        });
        this.packetTranslationMap.put(CPacketPlayerTryUseItemOnBlock.class, packet -> {
            // Note that CPacketPlayerTryUseItem is swapped with CPacketPlayerBlockPlacement
            final CPacketPlayerTryUseItemOnBlock blockPlace = (CPacketPlayerTryUseItemOnBlock) packet;
            final BlockPos blockPos = blockPlace.getPos();
            final EnumFacing front = blockPlace.getDirection();
            final MinecraftServer server = SpongeImpl.getServer();
            if (blockPos.getY() < server.getBuildLimit() - 1 || front != EnumFacing.UP && blockPos.getY() < server.getBuildLimit()) {
                return General.PLACE_BLOCK;
            }
            return General.INVALID;
        });
        this.packetTranslationMap.put(CPacketPlayerTryUseItem.class, packet -> General.USE_ITEM);
        this.packetTranslationMap.put(CPacketHeldItemChange.class, packet -> Inventory.SWITCH_HOTBAR_SCROLL);
        this.packetTranslationMap.put(CPacketAnimation.class, packet -> General.ANIMATION);
        this.packetTranslationMap.put(CPacketEntityAction.class, packet -> {
            final CPacketEntityAction playerAction = (CPacketEntityAction) packet;
            final CPacketEntityAction.Action action = playerAction.getAction();
            return PLAYER_ACTION_MAPPINGS.get(action);
        });
        this.packetTranslationMap.put(CPacketInput.class, packet -> General.HANDLED_EXTERNALLY);
        this.packetTranslationMap.put(CPacketCloseWindow.class, packet -> General.CLOSE_WINDOW);
        this.packetTranslationMap.put(CPacketClickWindow.class, packet -> fromWindowPacket((CPacketClickWindow) packet));
        this.packetTranslationMap.put(CPacketConfirmTransaction.class, packet -> General.UNKNOWN);
        this.packetTranslationMap.put(CPacketCreativeInventoryAction.class, packet -> General.CREATIVE_INVENTORY);
        this.packetTranslationMap.put(CPacketEnchantItem.class, packet -> Inventory.ENCHANT_ITEM);
        this.packetTranslationMap.put(CPacketUpdateSign.class, packet -> General.UPDATE_SIGN);
        this.packetTranslationMap.put(CPacketPlayerAbilities.class, packet -> General.IGNORED);
        this.packetTranslationMap.put(CPacketTabComplete.class, packet -> General.HANDLED_EXTERNALLY);
        this.packetTranslationMap.put(CPacketClientStatus.class, packet -> {
            final CPacketClientStatus clientStatus = (CPacketClientStatus) packet;
            final CPacketClientStatus.State status = clientStatus.getStatus();
            if (status == CPacketClientStatus.State.PERFORM_RESPAWN) {
                return General.REQUEST_RESPAWN;
            }
            return General.IGNORED;
        });
        this.packetTranslationMap.put(CPacketCustomPayload.class, packet -> General.HANDLED_EXTERNALLY);
        this.packetTranslationMap.put(CPacketSpectate.class, packet -> General.IGNORED);
        this.packetTranslationMap.put(CPacketResourcePackStatus.class, packet -> General.RESOURCE_PACK);
        this.packetTranslationMap.put(CPacketPlaceRecipe.class, packet -> Inventory.PLACE_RECIPE);
    }

    private static final ImmutableMap<CPacketEntityAction.Action, IPhaseState<? extends PacketContext<?>>> PLAYER_ACTION_MAPPINGS = ImmutableMap.<CPacketEntityAction.Action, IPhaseState<? extends PacketContext<?>>>builder()
            .put(CPacketEntityAction.Action.START_SNEAKING, General.START_SNEAKING)
            .put(CPacketEntityAction.Action.STOP_SNEAKING, General.STOP_SNEAKING)
            .put(CPacketEntityAction.Action.STOP_SLEEPING, General.STOP_SLEEPING)
            .put(CPacketEntityAction.Action.START_SPRINTING, General.START_SPRINTING)
            .put(CPacketEntityAction.Action.STOP_SPRINTING, General.STOP_SPRINTING)
            .put(CPacketEntityAction.Action.START_RIDING_JUMP, General.START_RIDING_JUMP)
            .put(CPacketEntityAction.Action.STOP_RIDING_JUMP, General.STOP_RIDING_JUMP)
            .put(CPacketEntityAction.Action.OPEN_INVENTORY, Inventory.OPEN_INVENTORY)
            .put(CPacketEntityAction.Action.START_FALL_FLYING, General.START_FALL_FLYING)
            .build();
    private static final ImmutableMap<CPacketPlayerDigging.Action, IPhaseState<? extends PacketContext<?>>> INTERACTION_ACTION_MAPPINGS = ImmutableMap.<CPacketPlayerDigging.Action, IPhaseState<? extends PacketContext<?>>>builder()
            .put(CPacketPlayerDigging.Action.DROP_ITEM, Inventory.DROP_ITEM_WITH_HOTKEY)
            .put(CPacketPlayerDigging.Action.DROP_ALL_ITEMS, Inventory.DROP_ITEM_WITH_HOTKEY)
            .put(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, General.INTERACTION)
            .put(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, General.INTERACTION)
            .put(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, General.INTERACTION)
            .put(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, General.INTERACTION)
            .put(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, Inventory.SWAP_HAND_ITEMS)
            .build();

}
