package me.vik1395.BungeeAuth;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/*

Author: Vik1395
Project: BungeeAuth

Copyright 2015

Licensed under Creative CommonsAttribution-ShareAlike 4.0 International Public License (the "License");
You may not use this file except in compliance with the License.

You may obtain a copy of the License at http://creativecommons.org/licenses/by-sa/4.0/legalcode

You may find an abridged version of the License at http://creativecommons.org/licenses/by-sa/4.0/
 */

public class ListenerClass implements Listener
{
	Tables ct = new Tables();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(PostLoginEvent ple)
	{
		ProxiedPlayer pl = ple.getPlayer();
		boolean check = ct.checkPlayerEntry(pl.getName());
		
		//Checks for player entry in Database
		if(check)
		{
			Date lastseen = ct.getLastSeen(pl.getName());
			String lastip = ct.getLastIP(pl.getName());
			String currip = pl.getAddress().getHostString();
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
			String sdatenow = df.format(new Date());
			Date datenow = null;
			try 
			{
				datenow = df.parse(sdatenow);
			} 
			catch (ParseException e) 
			{
				e.printStackTrace();
			}
			
			long difference = datenow.getTime() - lastseen.getTime();
			long diffmin = (difference/1000)/60;
			
			//checks if player's session is still available
			if(currip.equals(lastip) && diffmin<=Main.seshlength)
			{
					pl.sendMessage(new ComponentBuilder(Main.welcome_resume.replace("%player%", pl.getName())).color(ChatColor.GREEN).create());
					ct.setStatus(pl.getName(), "online");
					if(!Main.plonline.contains(pl.getName()))
					{
						Main.plonline.add(pl.getName());
					}

					movePlayer(pl, false);
			}
			else
			{
				if(Main.plonline.contains(pl.getName()))
				{
					Main.plonline.remove(pl.getName());
				}
				
				movePlayer(pl, true);
				pl.sendMessage(new ComponentBuilder(Main.welcome_login).color(ChatColor.RED).create());
			}
		}
		
		else if(!check)
		{
			movePlayer(pl, true);
			String emailCh = "";
			if(Main.email)
			{
				emailCh = " [email]";
			}
			pl.sendMessage(new ComponentBuilder(Main.welcome_register.replace("%player%", pl.getName().replace("%email%", emailCh))).color(ChatColor.RED).create());
			//pl.sendMessage(new ComponentBuilder("").color(ChatColor.RED).create());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChatEvent(ChatEvent event) {
	  ProxiedPlayer p = (ProxiedPlayer) event.getSender();
	  String msg = event.getMessage();
	  String arr[] = msg.split(" ");
	  String cmd = arr[0];
	  
	  if(!Main.plonline.contains(p.getName()) && !cmd.equalsIgnoreCase("/login") && !cmd.equalsIgnoreCase("/register"))
	  {
		  event.setCancelled(true);
		  p.sendMessage(new ComponentBuilder(Main.pre_login).color(ChatColor.GRAY).create());
	  }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerDisconnectEvent pde)
	{
		ProxiedPlayer pl = pde.getPlayer();
		if(Main.plonline.contains(pl.getName()))
		{
			Main.plonline.remove(pl.getName());
			ct.setLastSeen(pl.getName(), pl.getAddress().getAddress().getHostAddress(), null);
		}
		ct.setStatus(pl.getName(), "offline");
	}
	
	public static void movePlayer(ProxiedPlayer pl, boolean authlobby)
	{
		ProxyServer ps = Main.plugin.getProxy();
		
		if(authlobby)
		{
			if(!(ps.getServerInfo(Main.authlobby)==null))
			{
				ServerInfo sinf = ps.getServerInfo(Main.authlobby);
				pl.connect(sinf);
			}
			else if(!(ps.getServerInfo(Main.authlobby2)==null))
			{
				ServerInfo sinf = ps.getServerInfo(Main.authlobby2);
				pl.connect(sinf);
			}
			else
			{
				pl.sendMessage(new ComponentBuilder(Main.error_authlobby).color(ChatColor.DARK_RED).create());
				System.err.println("[BungeeAuth] AuthLobby and Fallback AuthLobby not found!");
			}
		}
		else
		{
			if(!(ps.getServerInfo(Main.lobby)==null))
			{
				ServerInfo sinf = ps.getServerInfo(Main.lobby);
				pl.connect(sinf);
			}
			else if(!(ps.getServerInfo(Main.lobby2)==null))
			{
				ServerInfo sinf = ps.getServerInfo(Main.lobby2);
				pl.connect(sinf);
			}
			else
			{
				pl.sendMessage(new ComponentBuilder(Main.error_lobby).color(ChatColor.DARK_RED).create());
				System.err.println("[BungeeAuth] Lobby and Fallback Lobby not found!");
			}
		}
	}
}