package com.extrahardmode.events;


import com.extrahardmode.features.monsters.skeletors.CustomSkeleton;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a Skeleton deflects an arrow, e.g doesn't take damage fom an Arrow
 *
 * @author Diemex
 */
public class EhmSkeletonDeflectEvent extends Event implements Cancellable
{
    private final CustomSkeleton type;
    private boolean cancelled;
    private final Player shooter;
    private final Skeleton skeleton;
    private final int deflectPercentage;


    /**
     * Constructor
     *
     * @param shooter           Player shooting the arrow
     * @param skeleton          Skeleton getting hit
     * @param deflectPercentage the percentage of arrows a Skeleton deflects
     */
    public EhmSkeletonDeflectEvent(Player shooter, Skeleton skeleton, int deflectPercentage, CustomSkeleton type)
    {
        this.shooter = shooter;
        this.skeleton = skeleton;
        this.deflectPercentage = deflectPercentage;
        this.type = type;
    }


    /**
     * Constructor
     *
     * @param shooter           Player shooting the arrow
     * @param skeleton          Skeleton getting hit
     * @param deflectPercentage the percentage of arrows a Skeleton deflects
     */
    public EhmSkeletonDeflectEvent(Player shooter, Skeleton skeleton, int deflectPercentage, CustomSkeleton type, boolean cancelled)
    {
        this(shooter, skeleton, deflectPercentage, type);
        this.cancelled = cancelled;
    }


    /**
     * @return the Skeleton that is being shot at
     */
    public Skeleton getSkeleton()
    {
        return skeleton;
    }


    /**
     * @return the percentage of arrows getting deflected
     */
    public int getDeflectPercentage()
    {
        return deflectPercentage;
    }


    /**
     * @return if Event got cancelled
     */
    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }


    /**
     * @param cancelled set if the Event is cancelled which mean that the Skeleton will take normal damage
     */
    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }


    /**
     * @return the Player shooting the Skeleton
     */
    public Player getShooter()
    {
        return shooter;
    }


    private static final HandlerList HANDLERS = new HandlerList();


    public HandlerList getHandlers()
    {
        return HANDLERS;
    }


    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }
}