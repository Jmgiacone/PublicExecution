package com.github.jdog653.publicexecution;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

/**
 * @author Jdog653
 */
public class PublicExecution extends JavaPlugin implements Listener
{
    private Location executionLoc;
    private ArrayList<String> toBeBanned;

    public PublicExecution()
    {
        executionLoc = null;
        toBeBanned = new ArrayList<>();
    }

    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable()
    {

    }

    @EventHandler
    public void tryToMove(PlayerMoveEvent e)
    {
        if(toBeBanned.contains(e.getPlayer().getName()))
        {
            e.setCancelled(true);
            e.getPlayer().sendMessage("You're to be executed, you're going nowhere!");
        }
    }

    @EventHandler
    public void banOnDeath(EntityDeathEvent e)
    {
        if(e.getEntity() instanceof Player)
        {
            Player p = (Player)e.getEntity();

            if(toBeBanned.contains(p.getName()))
            {
                p.setBanned(true);
                Bukkit.broadcastMessage(p.getName() + " has been banned!");
                toBeBanned.remove(p.getName());
                p.kickPlayer("You're stupid");
            }
        }

    }

    @EventHandler
    public void tryToLogout(PlayerQuitEvent e)
    {
        if(toBeBanned.contains(e.getPlayer().getName()) )
        {
            e.getPlayer().setBanned(true);
            Bukkit.broadcastMessage(e.getPlayer().getName() + " has been banned!");
            toBeBanned.remove(e.getPlayer().getName());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        switch (command.getName())
        {
            case "execute":
                if(args.length == 1)
                {
                    Player p = Bukkit.getServer().getPlayer(args[0]);
                    if(p != null)
                    {
                        try
                        {
                            p.teleport(executionLoc);
                        }
                        catch (NullPointerException e)
                        {
                            sender.sendMessage("The Execution Location has not been set! Set it with /setExecutionArea");
                            return true;
                        }

                        toBeBanned.add(p.getName());
                        Bukkit.broadcastMessage(p.getName() + " is about to be executed!");
                        return true;
                    }

                    sender.sendMessage(args[0] + " isn't online!");
                }
                return false;
            case "setexecutionarea":
                if(sender instanceof Player)
                {
                    if(args.length == 0)
                    {
                        executionLoc = ((Player)sender).getLocation();
                        sender.sendMessage("Execution Location has been set!");
                        return true;
                    }
                    return false;
                }

        }
        return true;
    }
}
