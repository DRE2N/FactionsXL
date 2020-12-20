/*
 * Copyright (C) 2017-2020 Daniel Saukel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.erethon.factionsxl.command;

import de.erethon.commons.command.DRECommandCache;
import de.erethon.commons.javaplugin.DREPlugin;
import de.erethon.factionsxl.FactionsXL;
import de.erethon.factionsxl.command.relation.*;
import de.erethon.factionsxl.command.war.*;
import de.erethon.factionsxl.config.FConfig;

/**
 * An enumeration of all command instances.
 *
 * @author Daniel Saukel
 */
public class FCommandCache extends DRECommandCache {

    public static final String LABEL = "factionsxl";

    DREPlugin plugin;

    public AddCasusBelliCommand addCasusBelli = new AddCasusBelliCommand();
    public AdminCommand admin = new AdminCommand();
    public CasusBelliCommand casusBelli = new CasusBelliCommand();
    public ChatCommand chat = new ChatCommand();
    public ChatSpyCommand chatSpy = new ChatSpyCommand();
    public ClaimCommand claim = new ClaimCommand();
    public CoreCommand core = new CoreCommand();
    public ConfirmPeaceRequestCommand confirmPeace = new ConfirmPeaceRequestCommand();
    public ConfirmWarCommand confirmWar = new ConfirmWarCommand();
    public ConfirmWarRequestCommand confirmWarRequest = new ConfirmWarRequestCommand();
    public CreateCommand create = new CreateCommand();
    public CreateBullCommand createBull = new CreateBullCommand();
    public CreateVassalCommand createVassal = new CreateVassalCommand();
    public DescCommand desc = new DescCommand();
    public DisbandCommand disband = new DisbandCommand();
    public GiveRegionCommand giveRegion = new GiveRegionCommand();
    public HelpCommand help = new HelpCommand();
    public HomeCommand home = new HomeCommand();
    public IdeaCommand idea = new IdeaCommand();
    public IndependenceCommand independence = new IndependenceCommand();
    public IntegrateCommand integrate = new IntegrateCommand();
    public InviteCommand invite = new InviteCommand();
    public JoinCommand join = new JoinCommand();
    public KickCommand kick = new KickCommand();
    public LeaveCommand leave = new LeaveCommand();
    public ListCommand list = new ListCommand();
    public LongTagCommand longTag = new LongTagCommand();
    public MainCommand main = new MainCommand();
    public MobCommand mob = new MobCommand();
    public ModCommand mod = new ModCommand();
    public MoneyCommand money = new MoneyCommand();
    public OpenCommand open = new OpenCommand();
    public PaydayCommand payday = new PaydayCommand();
    public PeaceCommand peaceCommand = new PeaceCommand();
    public PlayerHomeCommand playerHome = new PlayerHomeCommand();
    public PowerCommand power = new PowerCommand();
    public RegionCommand region = new RegionCommand();
    public RegionsCommand regions = new RegionsCommand();
    public RelationCommand relation = new RelationCommand();
    public RelationAllyCommand relationAlly = new RelationAllyCommand();
    public RelationOathCommand relationOath = new RelationOathCommand();
    public RelationNeutralCommand relationNeutral = new RelationNeutralCommand();
    public RelationUniteCommand relationUnite = new RelationUniteCommand();
    public RelationVassalizeCommand relationVassalize = new RelationVassalizeCommand();
    public ReloadCommand reload = new ReloadCommand();
    public RequestsCommand requests = new RequestsCommand();
    public ScoreboardCommand scoreboard = new ScoreboardCommand();
    public SetAnthemCommand setAnthem = new SetAnthemCommand();
    public SetBannerCommand setBanner = new SetBannerCommand();
    public SetCapitalCommand setCapital = new SetCapitalCommand();
    public SetColorCommand setColor = new SetColorCommand();
    public SetGovernmentCommand setGovernment = new SetGovernmentCommand();
    public SetHomeCommand setHome = new SetHomeCommand();
    public SetPlayerHomeCommand setPlayerHome = new SetPlayerHomeCommand();
    public SetPowerCommand setPower = new SetPowerCommand();
    public ShortTagCommand shortTag = new ShortTagCommand();
    public ShowCommand show = new ShowCommand();
    public StatsCommand stats = new StatsCommand();
    public StorageCommand storage = new StorageCommand();
    public TagCommand tag = new TagCommand();
    public TitleCommand title = new TitleCommand();
    public TogglePublicCommand togglePublic = new TogglePublicCommand();
    public TradeOfferCommand tradeOffer = new TradeOfferCommand();
    public UnclaimCommand unclaim = new UnclaimCommand();
    public UninviteCommand uninvite = new UninviteCommand();
    public WarCommand war = new WarCommand();
    public WarStatusCommand warStatus = new WarStatusCommand();
    public OccupyCommand warAnnex = new OccupyCommand();
    public WorldCommand world = new WorldCommand();

    public WarInviteCommand warInviteCommand = new WarInviteCommand();
    public WarAdminCommand editWar = new WarAdminCommand();

    public MenuCommand menuCommand = new MenuCommand();

    public FCommandCache(DREPlugin plugin) {
        super(LABEL, plugin);
        this.plugin = plugin;
        addCommand(addCasusBelli);
        addCommand(admin);
        addCommand(casusBelli);
        addCommand(chat);
        addCommand(claim);
        addCommand(confirmWar);
        addCommand(create);
        addCommand(createBull);
        addCommand(createVassal);
        addCommand(desc);
        addCommand(disband);
        addCommand(giveRegion);
        addCommand(help);
        addCommand(home);
        addCommand(integrate);
        addCommand(invite);
        addCommand(join);
        addCommand(kick);
        addCommand(leave);
        addCommand(list);
        addCommand(longTag);
        addCommand(main);
        addCommand(mob);
        addCommand(mod);
        addCommand(open);
        addCommand(peaceCommand);
        addCommand(power);
        addCommand(region);
        addCommand(regions);
        addCommand(relation);
        addCommand(relationAlly);
        addCommand(relationOath);
        addCommand(relationNeutral);
        addCommand(relationUnite);
        addCommand(relationVassalize);
        addCommand(reload);
        addCommand(requests);
        addCommand(scoreboard);
        addCommand(setAnthem);
        addCommand(setBanner);
        addCommand(setCapital);
        addCommand(setColor);
        addCommand(setGovernment);
        addCommand(setHome);
        addCommand(setPower);
        addCommand(shortTag);
        addCommand(show);
        addCommand(tag);
        addCommand(togglePublic);
        addCommand(title);
        addCommand(unclaim);
        addCommand(uninvite);
        addCommand(war);
        addCommand(warStatus);
        addCommand(world);
        // experimental commands
        addCommand(core);
        addCommand(storage);
        addCommand(tradeOffer);
        addCommand(addCasusBelli);
        addCommand(chatSpy);
        //addCommand(idea);
        addCommand(confirmPeace);
        addCommand(confirmWarRequest);
        addCommand(peaceCommand);
        addCommand(warAnnex);
        addCommand(independence);
        addCommand(stats);

        addCommand(menuCommand);

        // debug/Workaround
        addCommand(warInviteCommand);
        addCommand(editWar);

        FConfig config = FactionsXL.getInstance().getFConfig();
        if (config.isEconomyEnabled()) {
            addCommand(idea);
            addCommand(money);
            addCommand(payday);
            addCommand(storage);
            addCommand(tradeOffer);
        }
        if (config.arePlayerHomesEnabled()) {
            addCommand(playerHome);
            addCommand(setPlayerHome);
        }
    }

    public void registerAliases() {
        FCommandAlias alias = new FCommandAlias(this);
        plugin.getCommand("home").setExecutor(alias);
        plugin.getCommand("setHome").setExecutor(alias);
    }



}
