package noppes.npcs.constants;

public enum EnumPlayerPacket
{
	FollowerHire, 
	FollowerExtend, 
	Trader, 
	FollowerState, 
	Transport, 
	BankUnlock, 
	BankUpgrade, 
	Dialog, 
	QuestCompletion,
	CheckQuestCompletion, 
	BankSlotOpen, 
	FactionsGet, 
	MailGet, 
	MailDelete, 
	MailSend, 
	MailRead, 
	MailboxOpenMail, 
	SignSave, 
	SaveBook, 
	CompanionOpenInv, 
	RoleGet, 
	CompanionTalentExp, 
	MarkData, 
	LeftClick, 
	CloseGui, 
	CustomGuiClose, 
	CustomGuiButton, 
	CustomGuiScrollClick,
	// New
	QuestRemoveActive,
	QuestChooseReward,
	QuestCompletionReward,
	NpcVisualData,
	MousesPressed,
	IsMoved,
	WindowSize,
	GetGhostRecipe,
	TraderMarketBuy,
	TraderMarketSell,
	TraderMarketReset,
	TraderMarketRemove,
	TakeMoney,
	ScriptDataGetVar,
	CurrentLanguage,
	GetBuildData,
	KeyPressed,
	HudTimerEnd;
	
}
