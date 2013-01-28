package com.github.jdog653.publicexecution;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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
        if(getConfig().get("ExecutionLocation") != null)
        {
            double x = getConfig().getDouble("ExecutionLocation.X"),
                    y = getConfig().getDouble("ExecutionLocation.Y"),
                    z = getConfig().getDouble("ExecutionLocation.Z");

            World w = Bukkit.getWorld(getConfig().getString("ExecutionLocation.World"));
            executionLoc = new Location(w, x, y, z);

            if(getConfig().getString("ToBeExecuted") != null)
            {
                for(String s : getConfig().getString("ToBeExecuted").split(";"))
                {
                    toBeBanned.add(s);
                }
            }
        }
    }

    @Override
    public void onDisable()
    {

    }

    @EventHandler
    public void blockPlaced(BlockPlaceEvent e)
    {
        if(toBeBanned.contains(e.getPlayer().getName()))
        {
            e.setCancelled(true);
            e.getPlayer().sendMessage("You can't place blocks while awaiting execution!");
        }
    }

    @EventHandler
    public void blockBroken(BlockBreakEvent e)
    {
        if(toBeBanned.contains(e.getPlayer().getName()))
        {
            e.setCancelled(true);
            e.getPlayer().sendMessage("You can't break blocks while awaiting execution!");
        }
    }
    @EventHandler
    public void tryToMove(PlayerMoveEvent e)
    {

        if(toBeBanned.contains(e.getPlayer().getName()))
        {
            e.setTo(e.getFrom());
            e.getPlayer().sendMessage("You're to be executed, you're not going anywhere!");
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
                p.kickPlayer("Goodbye, " + p.getName() + "!");
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

    private String listMinus(String n)
    {
        String[] names = getConfig().getString("ToBeExecuted").split(";");
        String newNames = "";

        for(String s : names)
        {
            if(!s.equalsIgnoreCase(n))
            {
                newNames += s + ";";
            }
        }

        return newNames;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(command.getName().equalsIgnoreCase("pe"))
        {
            if(args.length > 0)
            {
                switch (args[0])
                {
                    case "execute":
                        if(sender.hasPermission("pe.execute"))
                        {
                            if(args.length == 2)
                            {
                                Player p = Bukkit.getServer().getPlayer(args[1]);

                                if(p != null)
                                {
                                    if(!toBeBanned.contains(p.getName()))
                                    {
                                        try
                                        {
                                            p.teleport(executionLoc);
                                        }
                                        catch (NullPointerException e)
                                        {
                                            sender.sendMessage("The Execution Location has not been set! Set it with /pe setarea");
                                            return true;
                                        }

                                        toBeBanned.add(p.getName());

                                        if(getConfig().get("ToBeExecuted") != null)
                                        {
                                            String[] names = getConfig().getString("ToBeExecuted").split(";");

                                            if(names.length != 0)
                                            {
                                                getConfig().set("ToBeExecuted", p.getName() + ";");
                                            }
                                            else
                                            {
                                                getConfig().set("ToBeExecuted",
                                                        getConfig().getString("ToBeExecuted") + p.getName() + ";");
                                            }
                                        }


                                        getConfig().set("ToBeExecuted", p.getName() + ";");
                                        saveConfig();
                                        Bukkit.broadcastMessage(p.getName() + " is about to be executed!");
                                    }
                                    else
                                    {
                                        sender.sendMessage(p.getName() + " is already on Death Row!");
                                    }
                                }
                                else
                                {
                                    sender.sendMessage(args[1] + " isn't online!");
                                }
                                return true;
                            }
                            else
                            {
                                sender.sendMessage("Please specify a Player!");
                                return false;
                            }
                        }
                        sender.sendMessage("You don't have permission to execute anyone...");
                        return true;
                    case "setarea":
                        if(sender instanceof Player)
                        {
                            if(sender.hasPermission("pe.setarea"))
                            {
                                if(args.length == 1)
                                {
                                    executionLoc = ((Player)sender).getLocation();
                                    getConfig().set("ExecutionLocation.World" , executionLoc.getWorld().getName());
                                    getConfig().set("ExecutionLocation.X" , executionLoc.getX());
                                    getConfig().set("ExecutionLocation.Y" , executionLoc.getY());
                                    getConfig().set("ExecutionLocation.Z" , executionLoc.getZ());
                                    getConfig().set("ExecutionLocation.Yaw" , executionLoc.getYaw());
                                    getConfig().set("ExecutionLocation.Pitch" , executionLoc.getPitch());

                                    saveConfig();
                                    sender.sendMessage("Execution Location has been set!");
                                }
                                else
                                {
                                    sender.sendMessage("Invalid number of arguments!");
                                    return false;
                                }
                            }
                            else
                            {
                                sender.sendMessage("You don't have permission to set / change the location of the executions!");
                            }
                        }
                        else
                        {
                            sender.sendMessage("You must be in-game to set the location!");
                        }
                        return true;

                    case "pardon":
                        if(sender.hasPermission("pe.pardon"))
                        {
                            if(args.length == 2)
                            {
                                Player p = Bukkit.getServer().getPlayer(args[1]);

                                if(p != null)
                                {
                                    if(toBeBanned.contains(p.getName()))
                                    {
                                        toBeBanned.remove(p.getName());
                                        if(listMinus(p.getName()).equals(""))
                                        {
                                            getConfig().set("ToBeExecuted", null);
                                        }
                                        else
                                        {
                                            getConfig().set("ToBeExecuted", listMinus(p.getName()));
                                        }
                                        saveConfig();
                                        Bukkit.broadcastMessage(p.getName() + " has been released!");
                                    }
                                    else
                                    {
                                        sender.sendMessage(p.getName() + " isn't on Death Row!");
                                    }
                                }
                                else
                                {
                                    sender.sendMessage(args[1] + " isn't online!");
                                }
                                return true;
                            }
                            else
                            {
                                sender.sendMessage("Please specify a Player!");
                                return false;
                            }
                        }
                        else
                        {
                            sender.sendMessage("You don't have permission to pardon anybody!");
                            return true;
                        }
                }
                sender.sendMessage("Invalid Command!");
                return false;
            }
            sender.sendMessage("You must supply a command!");
            return false;
        }
        return true;
    }
}
