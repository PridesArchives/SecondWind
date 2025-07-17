package me.Pride.korra.SecondWind;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import commonslang3.projectkorra.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class SecondWind extends AirAbility implements AddonAbility, PassiveAbility {
	private final String config = "ExtraAbilities.Prride.SecondWind.";

	@com.projectkorra.projectkorra.attribute.Attribute(com.projectkorra.projectkorra.attribute.Attribute.COOLDOWN)
	private long cooldown;
	private double endurance;
	private double knockbackResistance;
	private double movementSpeed;
	private double oxygenBonus;
	private double waterMovementEfficiency;
	// private double absorption;

	public SecondWind(Player player) {
		super(player);

		if (CoreAbility.hasAbility(player, SecondWind.class)) {
			return;
		}
		this.cooldown = ConfigManager.getConfig().getLong(config + "Cooldown");
		this.endurance = ConfigManager.getConfig().getDouble(config + "Endurance");
		this.knockbackResistance = ConfigManager.getConfig().getDouble(config + "KnockbackResistanceModifier");
		this.movementSpeed = ConfigManager.getConfig().getDouble(config + "MovementSpeedModifier");
		this.oxygenBonus = ConfigManager.getConfig().getDouble(config + "OxygenBonus");
		this.waterMovementEfficiency = ConfigManager.getConfig().getDouble(config + "WaterMovementEfficiencyModifier");
		// this.absorption = ConfigManager.getConfig().getDouble(config + "Absorption");

		start();
	}

	@Override
	public void progress() { }

	public void activate() {
		bPlayer.addCooldown(this);

		player.sendMessage(Element.AIR.getColor() + "You gained a Second Wind!");

		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.75F, 0.5F);
		player.getWorld().spawnParticle(Particle.GUST_EMITTER_SMALL, player.getLocation(), 1, 0.5, 0.5, 0.5, 0.1);

		long start = System.currentTimeMillis();

		Tuple<Attribute, Double, Double>[] attributes = new Tuple[]{
			Tuple.of(Attribute.KNOCKBACK_RESISTANCE,
					player.getAttribute(Attribute.KNOCKBACK_RESISTANCE).getValue(),
					player.getAttribute(Attribute.KNOCKBACK_RESISTANCE).getBaseValue() + knockbackResistance),
			Tuple.of(Attribute.MOVEMENT_SPEED,
					player.getAttribute(Attribute.MOVEMENT_SPEED).getValue(),
					player.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue() + movementSpeed),
			Tuple.of(Attribute.OXYGEN_BONUS,
					player.getAttribute(Attribute.OXYGEN_BONUS).getValue(),
					player.getAttribute(Attribute.OXYGEN_BONUS).getBaseValue() + oxygenBonus),
			Tuple.of(Attribute.WATER_MOVEMENT_EFFICIENCY,
					player.getAttribute(Attribute.WATER_MOVEMENT_EFFICIENCY).getValue(),
					player.getAttribute(Attribute.WATER_MOVEMENT_EFFICIENCY).getBaseValue() + waterMovementEfficiency)
		};

		double absorptionValue = player.getAbsorptionAmount();

		for (Tuple<Attribute, Double, Double> attribute : attributes) {
			if (attribute.getSecond() < attribute.getThird()) {
				player.getAttribute(attribute.getFirst()).setBaseValue(attribute.getSecond() + attribute.getThird());
			}
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				if (System.currentTimeMillis() > start + ConfigManager.getConfig().getLong(config + "Duration")) {
					end();
					return;
				}
				if (!player.isOnline() || player.isDead()) {
					end();
					return;
				}
				if (!bPlayer.isElementToggled(Element.AIR) || !bPlayer.isPassiveToggled(Element.AIR) || !CoreAbility.hasAbility(player, SecondWind.class)) {
					end();
					return;
				}
			}
			private void end() {
				for (Tuple<Attribute, Double, Double> attribute : attributes) {
					player.getAttribute(attribute.getFirst()).setBaseValue(attribute.getSecond());
				}
				player.sendMessage(Element.AIR.getSubColor() + "Your Second Wind has ended.");
				this.cancel();
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);

		return;
	}

	public static void activate(Player player) {
		if (CoreAbility.hasAbility(player, SecondWind.class)) {
			CoreAbility.getAbility(player, SecondWind.class).activate();
		}
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "SecondWind";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public void load() {
		ProjectKorra.log.info("Succesfully loaded " + getName() + ": " + getVersion() + ", by " + getAuthor() + "!");
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new SecondWindListener(), ProjectKorra.plugin);

		ConfigManager.getConfig().addDefault("ExtraAbilities.Prride.SecondWind.Cooldown", 30000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Prride.SecondWind.Duration", 10000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Prride.SecondWind.Endurance", 50);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Prride.SecondWind.Chance", 20);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Prride.SecondWind.HealthThreshold", 10.0);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Prride.SecondWind.DamageThreshold", 4.0);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Prride.SecondWind.KnockbackResistanceModifier", 0.5);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Prride.SecondWind.MovementSpeedModifier", 0.02);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Prride.SecondWind.OxygenBonus", 512);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Prride.SecondWind.WaterMovementEfficiencyModifier", 0.2);

		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Stopped " + getAuthor() + "'s " + getName() + "!");
	}

	@Override
	public String getAuthor() {
		return Element.AIR.getSubColor() + "Prride";
	}

	@Override
	public String getVersion() {
		return Element.AIR.getSubColor() + "1.0";
	}

	@Override
	public String getDescription() {
		return Element.AIR.getSubColor() + "Airbenders are among the most defensively unrelenting of all benders due to their bending practices and extraordinary endurance. " +
				"SecondWind is a passive ability that grants you a burst of speed and a defensive cladding when your health is low. " +
				"You gain increased knockback resistance, movement speed, oxygen bonus, and water movement efficiency for a short duration, while the attack that " +
				"triggered the ability is endured.";
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isInstantiable() {
		return true;
	}

	@Override
	public boolean isProgressable() {
		return true;
	}

	static class Tuple<T, U, V> {
		private T first;
		private U second;
		private V third;

		public Tuple(T first, U second, V third) {
			this.first = first;
			this.second = second;
			this.third = third;
		}

		public T getFirst() {
			return this.first;
		}

		public U getSecond() {
			return this.second;
		}

		public V getThird() {
			return this.third;
		}

		public static <T, U, V> Tuple<T, U, V> of(T first, U second, V third) {
			return new Tuple<>(first, second, third);
		}
	}
}

class SecondWindListener implements Listener {
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.isCancelled() || event.getDamage() <= 0 || !(event.getEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getEntity();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (bPlayer == null || !CoreAbility.hasAbility(player, SecondWind.class)) {
			return;
		}
		if (bPlayer.isElementToggled(Element.EARTH) && CoreAbility.hasAbility(player, EarthArmor.class)) {
			return;
		}
		double chance = ConfigManager.getConfig().getInt("ExtraAbilities.Prride.SecondWind.Chance");
		double healthThreshold = ConfigManager.getConfig().getDouble("ExtraAbilities.Prride.SecondWind.HealthThreshold");
		double damageThreshold = ConfigManager.getConfig().getDouble("ExtraAbilities.Prride.SecondWind.DamageThreshold");
		double endurance = ConfigManager.getConfig().getDouble("ExtraAbilities.Prride.SecondWind.Endurance");

		if (player.getHealth() <= healthThreshold) {
			if (ThreadLocalRandom.current().nextInt(100) <= chance) {
				if (event.getDamage() >= damageThreshold) {
					event.setDamage(event.getDamage() / (100.0 / endurance));
					SecondWind.activate(player);
					return;
				}
			}
		}
	}
}