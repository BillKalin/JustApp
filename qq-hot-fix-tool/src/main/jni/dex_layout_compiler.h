//
// Created by Administrator on 2021.04.23.
//

#ifndef JUSTAPP_DEX_LAYOUT_COMPILER_H
#define JUSTAPP_DEX_LAYOUT_COMPILER_H

#include <codecvt>
#include <locale>
#include <string>
#include <vector>


// This visitor does the actual view compilation, using a supplied builder.
template<typename Builder>
class LayoutCompilerVisitor {
public:

    explicit LayoutCompilerVisitor(Builder *builder) : builder_{builder} {}

    void VisitStartDocument() { builder_->Start(); }

    void VisitEndDocument() { builder_->Finish(); }

    void VisitStartTag(const std::u16string &name) {
        parent_stack_.push_back(ViewEntry{
                std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t>{}.to_bytes(
                        name), {}});
    }

    void VisitEndTag() {
        auto entry = parent_stack_.back();
        parent_stack_.pop_back();

        if (parent_stack_.empty()) {
            GenerateCode(entry);
        } else {
            parent_stack_.back().children.push_back(entry);
        }
    }

private:
    struct ViewEntry {
        std::string name;
        std::vector<ViewEntry> children;
    };

    void GenerateCode(const ViewEntry &view) {
        builder_->StartView(view.name, !view.children.empty());
        for (const auto &child : view.children) {
            GenerateCode(child);
        }
        builder_->FinishView();
    }

    Builder *builder_;

    std::vector<ViewEntry> parent_stack_;
};

#endif //JUSTAPP_DEX_LAYOUT_COMPILER_H
