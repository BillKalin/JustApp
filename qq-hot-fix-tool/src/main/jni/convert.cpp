//
// Created by Administrator on 2021.04.23.
//

#include "convert.h"
#include "dex_layout_compiler.h"
#include "util.h"

template<typename Visitor>
class XmlVisitorAdapter : public XMLVisitor {
public:
    explicit XmlVisitorAdapter(Visitor *visitor) : visitor_{visitor} {}

    bool VisitEnter(const XMLDocument & /*doc*/) override {
        visitor_->VisitStartDocument();
        return true;
    }

    bool VisitExit(const XMLDocument & /*doc*/) override {
        visitor_->VisitEndDocument();
        return true;
    }

    bool VisitEnter(const XMLElement &element, const XMLAttribute * /*firstAttribute*/) override {
        visitor_->VisitStartTag(
                std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t>{}.from_bytes(
                        element.Name()));
        return true;
    }

    bool VisitExit(const XMLElement & /*element*/) override {
        visitor_->VisitEndTag();
        return true;
    }

private:
    Visitor *visitor_;
};

template<typename Builder>
void CompileLayout(XMLDocument *xml, Builder *builder) {
    LayoutCompilerVisitor<Builder> visitor{builder};
    XmlVisitorAdapter<decltype(visitor)> adapter{&visitor};
    xml->Accept(&adapter);
}

void
convert(const char *filepath, const char *layoutname, const char *pkg, const char *outFileName) {
    const char *filename = filepath;
    const char *layout_name = layoutname;
    std::string package = pkg;

    std::ofstream outfile;
    outfile.open(outFileName);

    bool is_stdout = 0;
    XMLDocument xml;
    xml.LoadFile(filename);

    // Generate Java language output.
    JavaLangViewBuilder builder{package, layout_name, is_stdout ? std::cout : outfile};

    CompileLayout(&xml, &builder);
}