package ifmo.poster.event.mapper;

import ifmo.poster.event.dto.CreateTagRequest;
import ifmo.poster.event.dto.TagResponse;
import ifmo.poster.event.dto.UpdateTagRequest;
import ifmo.poster.event.entity.Tag;

public final class TagMapper {

    private TagMapper() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static Tag toEntity(CreateTagRequest request) {
        Tag tag = new Tag();
        tag.setName(request.name().trim());
        return tag;
    }

    public static void updateEntity(Tag tag, UpdateTagRequest request) {
        tag.setName(request.name().trim());
    }

    public static TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }
}
