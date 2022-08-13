package fr.dynamx.server.command;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.objects.PhysicsRigidBody;
import fr.dynamx.api.physics.BulletShapeType;
import fr.dynamx.api.physics.IPhysicsWorld;
import fr.dynamx.common.DynamXContext;
import fr.dynamx.common.physics.entities.modules.EnginePhysicsHandler;
import fr.dynamx.common.physics.terrain.cache.TerrainFile;
import fr.dynamx.common.physics.terrain.element.CompoundBoxTerrainElement;
import fr.dynamx.common.physics.world.BasePhysicsWorld;
import fr.dynamx.utils.DynamXConfig;
import fr.dynamx.utils.DynamXConstants;
import fr.dynamx.utils.optimization.PooledHashMap;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DynamXConstants.ID)
public class DynamXCommands extends CommandBase
{
    private final Map<String, ISubCommand> commands = new HashMap<>();

    public static float explosionForce = 10;

    public DynamXCommands() {
        addCommand(new CmdSlopes());
        addCommand(new CmdReloadConfig());
        addCommand(new CmdRefreshChunks());
        addCommand(new CmdNetworkConfig());
        addCommand(new CmdChunkControl());
        addCommand(new CmdSpawnObjects());
        addCommand(new CmdKillEntities());
        addCommand(new CmdOpenDebugGui());
        addCommand(new ISubCommand() {
            @Override
            public String getName() {
                return "shockwave";
            }

            @Override
            public String getUsage() {
                return "shockwave <force> - Changes shockwave force";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                if(args.length == 2) {
                    explosionForce = (float) parseDouble(args[1]);
                    sender.sendMessage(new TextComponentString("Set force to "+explosionForce));
                }
                else
                    throw new WrongUsageException(getUsage());
            }
        });
        addCommand(new ISubCommand() {
            @Override
            public String getName() {
                return "testfullgo";
            }

            @Override
            public String getUsage() {
                return "testfullgo <false/true> - ready for speed ?";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                if(args.length == 2) {
                    boolean testFullGo = parseBoolean(args[1]);
                    EnginePhysicsHandler.inTestFullGo = testFullGo;
                    sender.sendMessage(new TextComponentString("Set test full go to "+testFullGo));
                }
                else
                    throw new WrongUsageException(getUsage());
            }
        });
        addCommand(new ISubCommand() {
            @Override
            public String getName() {
                return "terrain_debug";
            }

            @Override
            public String getUsage() {
                return "terrain_debug <false/true> - enables terrain debug";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                if(args.length == 2) {
                    boolean testFullGo = parseBoolean(args[1]);
                    DynamXConfig.enableDebugTerrainManager = testFullGo;
                    sender.sendMessage(new TextComponentString("Set terrain debug to "+testFullGo));
                }
                else
                    throw new WrongUsageException(getUsage());
            }
        });
        addCommand(new ISubCommand() {
            @Override
            public String getName() {
                return "bigdebugterrain";
            }

            @Override
            public String getUsage() {
                return "bigdebugterrain <false/true> - ready for debug and console spam ?";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                if(args.length == 2) {
                    boolean testFullGo = parseBoolean(args[1]);
                    TerrainFile.ULTIMATEDEBUG = testFullGo;
                    sender.sendMessage(new TextComponentString("Set bigdebugterrain to "+testFullGo));
                }
                else
                    throw new WrongUsageException(getUsage());
            }
        });
        addCommand(new ISubCommand() {
            @Override
            public String getName() {
                return "debug";
            }

            @Override
            public String getUsage() {
                return "debug halt";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                if(args.length < 1) {
                    sender.sendMessage(new TextComponentString(".dynamx [debug, info]"));
                    return;
                }
                if(args[0].equalsIgnoreCase("debug")) {
                    if(args.length < 2) {
                        sender.sendMessage(new TextComponentString(".dynamx debug [statistics]"));
                        return;
                    }
                    if(args[1].equalsIgnoreCase("statistics")) {
                        try {
                            IPhysicsWorld iPhysicsWorld = DynamXContext.getPhysicsWorld();
                            if(iPhysicsWorld instanceof BasePhysicsWorld) {
                                BasePhysicsWorld world = (BasePhysicsWorld) iPhysicsWorld;
                                sender.sendMessage(new TextComponentString("---------"));
                                sender.sendMessage(new TextComponentString(world.getCollisionObjects().size() + " Collision Objects"));
                                sender.sendMessage(new TextComponentString(world.getVehicles().size() + " Vehicle"));
                                sender.sendMessage(new TextComponentString(world.getEntities().size() + " Entities"));
                                sender.sendMessage(new TextComponentString(world.getJoints().size() + " Joints"));
                                sender.sendMessage(new TextComponentString(DynamXContext.getPlayerToCollision().size() + " Collision Player"));
                            } else {
                                sender.sendMessage(new TextComponentString("Physics World != Base Physics World ):"));
                            }
                        } catch(Exception exception) {
                            sender.sendMessage(new TextComponentString("fehler aufgetreten du hurensohn"));
                        }
                    } else if(args[1].equalsIgnoreCase("info")) {
                        if(args.length < 3) {
                            sender.sendMessage(new TextComponentString(".dynamx debug info [collisionobjects]"));
                            return;
                        }
                        if(args[2].equalsIgnoreCase("collisionobjects")) {
                            try {
                                IPhysicsWorld iPhysicsWorld = DynamXContext.getPhysicsWorld();
                                if(iPhysicsWorld instanceof BasePhysicsWorld) {
                                    BasePhysicsWorld world = (BasePhysicsWorld) iPhysicsWorld;
                                    List<PhysicsCollisionObject> objects = new ArrayList<>(world.getCollisionObjects());
                                    sender.sendMessage(new TextComponentString("size -> " + objects.size()));
                                    for(PhysicsCollisionObject object : objects) {
                                        if(object instanceof PhysicsRigidBody) {
                                            PhysicsRigidBody body = (PhysicsRigidBody) object;
                                            Object userObject = body.getUserObject();
                                            if(userObject instanceof BulletShapeType<?>) {
                                                BulletShapeType<?> bulletShapeType = (BulletShapeType<?>) userObject;
                                                sender.sendMessage(new TextComponentString("PhysicsRigedBody | UserObject -> " + bulletShapeType.getType().name() + " | " + bulletShapeType.getObjectIn().getClass().getSimpleName()));
                                                //System.out.println("PhysicsRigedBody | UserObject -> " + bulletShapeType.getType().name() + " | " + bulletShapeType.getObjectIn().getClass().getSimpleName());
                                            }
                                            continue;
                                        }

                                        sender.sendMessage(new TextComponentString("nativeId -> " + object.nativeId() + " | class = " + object.getClass().getSimpleName()));
                                    }
                                } else {
                                    sender.sendMessage(new TextComponentString("Physics World != Base Physics World ):"));
                                }
                            } catch(Exception ignored) {
                                sender.sendMessage(new TextComponentString("fehler aufgetreten du hurensohn"));
                            }
                        } else if(args[2].equalsIgnoreCase("terrain")) {
                            try {
                                IPhysicsWorld iPhysicsWorld = DynamXContext.getPhysicsWorld();
                                if(iPhysicsWorld instanceof BasePhysicsWorld) {
                                    BasePhysicsWorld world = (BasePhysicsWorld) iPhysicsWorld;
                                    List<PhysicsCollisionObject> objects = new ArrayList<>(world.getCollisionObjects());
                                    sender.sendMessage(new TextComponentString("size -> " + objects.size()));
                                    for(PhysicsCollisionObject object : objects) {
                                        if(object instanceof PhysicsRigidBody) {
                                            PhysicsRigidBody body = (PhysicsRigidBody) object;
                                            Object userObject = body.getUserObject();
                                            if(userObject instanceof BulletShapeType<?>) {
                                                BulletShapeType<?> bulletShapeType = (BulletShapeType<?>) userObject;
                                                if(bulletShapeType.getObjectIn() instanceof CompoundBoxTerrainElement) {
                                                    CompoundBoxTerrainElement element = (CompoundBoxTerrainElement) bulletShapeType.getObjectIn();
                                                    element.toString();
                                                }
                                            };
                                        }
                                    }
                                } else {
                                    sender.sendMessage(new TextComponentString("Physics World != Base Physics World ):"));
                                }
                            } catch(Exception ignored) {
                                sender.sendMessage(new TextComponentString("fehler aufgetreten du hurensohn"));
                            }
                        }
                    }
                }
            }
        });
        addCommand(new ISubCommand() {
            @Override
            public String getName() {
                return "disablemappool";
            }

            @Override
            public String getUsage() {
                return "disablemappool <false/true> - tests for hash map pool optimizations";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                if(args.length == 2) {
                    boolean testFullGo = parseBoolean(args[1]);
                    PooledHashMap.DISABLE_POOL = testFullGo;
                    sender.sendMessage(new TextComponentString("Set disablemappool to "+testFullGo));
                }
                else
                    throw new WrongUsageException(getUsage());
            }
        });
        addCommand(new CmdPhysicsMode());
    }

    public void addCommand(ISubCommand command) {
        commands.put(command.getName(), command);
        PermissionAPI.registerNode(command.getPermission(), DefaultPermissionLevel.OP, "/dynamx "+command.getUsage());
    }

    @Override
    public String getName() {
        return "dynamx";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        StringBuilder usage = new StringBuilder();
        commands.keySet().forEach(s -> usage.append("|").append(s));
        return "/dynamx <"+ usage.substring(1)+">";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0 && commands.containsKey(args[0])) {
            ISubCommand command = commands.get(args[0]);
            if(!(sender instanceof EntityPlayer) || PermissionAPI.hasPermission((EntityPlayer) sender, command.getPermission())) {
                command.execute(server, sender, args);
            }
            else {
                throw new CommandException("You don't have permission to use this command !");
            }
        }
        else
            throw new WrongUsageException(this.getUsage(sender));
    }

    public EntityPlayerMP getPlayer(String name) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(name);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> r = new ArrayList<String>();
        if (args.length == 1) {
            r.addAll(commands.keySet());
        }
        else if(args.length > 1 && commands.containsKey(args[0])) {
            commands.get(args[0]).getTabCompletions(server, sender, args, targetPos, r);
        }
        return getListOfStringsMatchingLastWord(args, r);
    }
}
