package ninja.amp.falloutwebserver.server.servlet;

import ninja.amp.fallout.Fallout;
import ninja.amp.fallout.FalloutCore;
import ninja.amp.fallout.character.Character;
import ninja.amp.fallout.character.CharacterManager;
import ninja.amp.fallout.character.Perk;
import ninja.amp.fallout.character.Skill;
import ninja.amp.fallout.character.Special;
import ninja.amp.fallout.character.Trait;
import ninja.amp.fallout.command.commands.character.knowledge.Information;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProfileServlet extends HttpServlet {

    private FalloutCore fallout;

    public ProfileServlet(FalloutCore fallout) {
        this.fallout = fallout;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CharacterManager characterManager = fallout.getCharacterManager();

        String characterName = request.getParameter("character");
        Character character = null;
        if (characterName != null && !characterName.isEmpty()) {
            if (characterManager.isCharacter(characterName)) {
                if (characterManager.isLoaded(characterName)) {
                    character = characterManager.getCharacterByName(characterName);
                } else {
                    character = characterManager.loadOfflineCharacter(characterName);
                }
            } else {
                for (Map.Entry<UUID, String> player : Fallout.getOnlinePlayers().entrySet()) {
                    if (player.getValue().equalsIgnoreCase(characterName)) {
                        UUID playerId = player.getKey();
                        if (characterManager.isOwner(playerId)) {
                            character = characterManager.getCharacterByOwner(playerId);
                        }
                        break;
                    }
                }
            }
        }

        if (character != null) {
            request.setAttribute("characterName", character.getCharacterName().replace('_', ' '));
            String ownerName = character.getOwnerName();
            request.setAttribute("ownerName", ownerName == null ? "abandoned" : ownerName);
            request.setAttribute("avatarName", ownerName == null ? "steve" : ownerName);

            request.setAttribute("gender", character.getGender() == Character.Gender.MALE ? "Male" : "Female");
            request.setAttribute("race", character.getRace().getName());
            request.setAttribute("alignment", character.getAlignment().getName());
            request.setAttribute("age", character.getAge());
            request.setAttribute("height", character.getHeight());
            request.setAttribute("weight", character.getWeight());

            request.setAttribute("level", character.getLevel());
            List<String> perks = new ArrayList<>();
            for (Perk perk : character.getPerks()) {
                perks.add("Tier " + perk.getTier() + ": " + perk.getName());
            }
            request.setAttribute("perks", perks);

            String faction = character.getFaction();
            request.setAttribute("faction", faction == null ? "No Allegiance" : faction);

            List<String> knowledge = new ArrayList<>();
            for (String information : Information.getInformationPieces()) {
                if (character.hasKnowledge(information)) {
                    knowledge.add(information);
                }
            }
            request.setAttribute("knowledge", knowledge);

            Special special = character.getSpecial();
            for (Trait trait : Trait.class.getEnumConstants()) {
                request.setAttribute(trait.getName().toLowerCase(), special.get(trait));
            }

            for (Skill skill : Skill.class.getEnumConstants()) {
                request.setAttribute(skill.getName().toLowerCase(), character.skillLevel(skill));
            }
        }

        request.getRequestDispatcher("/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
