package earth.terrarium.adastra.datagen.builder;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import earth.terrarium.adastra.AdAstra;
import earth.terrarium.adastra.common.recipes.SpaceStationRecipe;
import earth.terrarium.adastra.common.recipes.base.IngredientHolder;
import earth.terrarium.adastra.common.registry.ModRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class SpaceStationRecipeBuilder implements RecipeBuilder {
    private final Advancement.Builder advancement = Advancement.Builder.advancement();

    private final List<IngredientHolder> ingredients;
    private final ResourceKey<Level> dimension;
    private final ResourceLocation structure;

    public SpaceStationRecipeBuilder(List<IngredientHolder> ingredients, ResourceKey<Level> dimension, ResourceLocation structure) {
        this.ingredients = ingredients;
        this.dimension = dimension;
        this.structure = structure;
    }

    @Override
    public @NotNull SpaceStationRecipeBuilder unlockedBy(@NotNull String criterionName, @NotNull CriterionTriggerInstance criterionTrigger) {
        this.advancement.addCriterion(criterionName, criterionTrigger);
        return this;
    }

    @Override
    public @NotNull SpaceStationRecipeBuilder group(@Nullable String groupName) {
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return Items.AIR;
    }

    @Override
    public void save(@NotNull Consumer<FinishedRecipe> finishedRecipeConsumer, @NotNull ResourceLocation recipeId) {
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT)
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
            .rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(recipeId))
            .requirements(RequirementsStrategy.OR);

        finishedRecipeConsumer.accept(new SpaceStationRecipeBuilder.Result(
            recipeId,
            ingredients, dimension, structure,
            this.advancement, new ResourceLocation(recipeId.getNamespace(), "recipes/space_stations/" + recipeId.getPath()))
        );
    }

    public record Result(
        ResourceLocation id,
        List<IngredientHolder> ingredients,
        ResourceKey<Level> dimension,
        ResourceLocation structure,
        Advancement.Builder advancement, ResourceLocation advancementId
    ) implements FinishedRecipe {

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            SpaceStationRecipe.codec(id)
                .encodeStart(JsonOps.INSTANCE, new SpaceStationRecipe(id, ingredients, dimension, structure))
                .resultOrPartial(AdAstra.LOGGER::error)
                .ifPresent(out ->
                    out.getAsJsonObject().entrySet().forEach(entry -> json.add(entry.getKey(), entry.getValue()))
                );
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return ModRecipeSerializers.SPACE_STATION_SERIALIZER.get();
        }

        @Override
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}
